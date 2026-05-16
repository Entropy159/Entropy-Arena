package com.entropy.arena.api.map;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.SpawnpointBlock;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.network.toClient.TakeScreenshotPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArenaMap {
    private boolean enabled;
    private final String name;
    private final ResourceLocation gamemodeID;
    private final BlockPos corner1;
    private final BlockPos corner2;
    private long time;
    private boolean raining;
    private boolean thundering;
    private MapScreenshot screenshot;
    private int timerOverride;
    private int targetScoreOverride;
    protected final HashMap<Property<?>, HashMap<Object, ArrayList<BlockPos>>> blockPropertyMap = new HashMap<>();

    public ArenaMap(ServerLevel level, String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2) {
        this(name, true, gamemodeID, BlockPos.min(corner1, corner2), BlockPos.max(corner1, corner2), level.getDayTime(), level.isRaining(), level.isThundering(), new MapScreenshot(name), 0, 0);
    }

    public ArenaMap(String name, boolean enabled, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2, long time, boolean raining, boolean thundering, MapScreenshot screenshot, int timerOverride, int targetScoreOverride) {
        this.name = name;
        this.enabled = enabled;
        this.gamemodeID = gamemodeID;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.time = time;
        this.raining = raining;
        this.thundering = thundering;
        this.screenshot = screenshot;
        this.timerOverride = timerOverride;
        this.targetScoreOverride = targetScoreOverride;
    }

    public ArrayList<ArenaTeam> getTeams(ServerLevel level) {
        return new ArrayList<>(getSpawns(level).keySet().stream().filter(t -> t != ArenaTeam.NONE).sorted(Comparator.comparingInt(Enum::ordinal)).toList());
    }

    public HashMap<ArenaTeam, ArrayList<BlockPos>> getSpawns(ServerLevel level) {
        return getBlockPropertyMap(level, SpawnpointBlock.SPAWN_COLOR);
    }

    private void forEachBlock(BiConsumer<Vec3i, BlockPos> function) {
        Vec3i size = corner2.subtract(corner1);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    function.accept(new Vec3i(x, y, z), corner1.offset(x, y, z));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> HashMap<T, ArrayList<BlockPos>> getBlockPropertyMap(ServerLevel level, Property<T> property) {
        if (ArenaData.get(level).backupState != ArenaMapBackup.BackupState.HAS_BACKUP) {
            HashMap<T, ArrayList<BlockPos>> map = new HashMap<>();
            forEachBlock((offset, pos) -> {
                if (level.getBlockState(pos).hasProperty(property)) {
                    map.computeIfAbsent(level.getBlockState(pos).getValue(property), v -> new ArrayList<>()).add(pos);
                }
            });
            return map;
        }
        return (HashMap<T, ArrayList<BlockPos>>) (HashMap<?, ?>) blockPropertyMap.get(property);
    }

    public @Nullable ArenaGamemode getNewGamemode() {
        return GamemodeRegistry.getNew(gamemodeID);
    }

    public ResourceLocation getGamemodeID() {
        return gamemodeID;
    }

    public void update(ServerLevel level, ServerPlayer player) {
        time = level.getDayTime();
        raining = level.isRaining();
        thundering = level.isThundering();
        PacketDistributor.sendToPlayer(player, new TakeScreenshotPacket(name));
    }

    public void setOverrides(int timerOverride, int targetScoreOverride) {
        this.timerOverride = timerOverride;
        this.targetScoreOverride = targetScoreOverride;
    }

    public void setScreenshot(MapScreenshot newScreenshot) {
        screenshot = newScreenshot;
    }

    public void load(ServerLevel level) {
        level.setWeatherParameters(99999, 99999, raining, thundering);
        level.setDayTime(time);
    }

    public void forEachChunk(Consumer<ChunkPos> consumer) {
        for (int x = SectionPos.blockToSectionCoord(corner1.getX()); x <= SectionPos.blockToSectionCoord(corner2.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(corner1.getZ()); z <= SectionPos.blockToSectionCoord(corner2.getZ()); z++) {
                consumer.accept(new ChunkPos(x, z));
            }
        }
    }

    public void reset(ServerLevel level, Runnable after) {
        if (ArenaData.get(level).backupState != ArenaMapBackup.BackupState.HAS_BACKUP) {
            after.run();
            return;
        }
        ArenaData.get(level).restoreBackup(after);
        blockPropertyMap.clear();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("gamemode", gamemodeID.toString());
        tag.putLong("corner1", corner1.asLong());
        tag.putLong("corner2", corner2.asLong());
        tag.putLong("time", time);
        tag.putBoolean("raining", raining);
        tag.putBoolean("thundering", thundering);
        tag.putByteArray("screenshot", screenshot.getData());
        tag.putInt("timerOverride", timerOverride);
        tag.putInt("targetScoreOverride", targetScoreOverride);
        return tag;
    }

    public static ArenaMap fromTag(CompoundTag tag) {
        String name = tag.getString("name");
        boolean enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        ResourceLocation gamemode = ResourceLocation.tryParse(tag.getString("gamemode"));
        BlockPos corner1 = BlockPos.of(tag.getLong("corner1"));
        BlockPos corner2 = BlockPos.of(tag.getLong("corner2"));
        long time = tag.getLong("time");
        boolean raining = tag.getBoolean("raining");
        boolean thundering = tag.getBoolean("thundering");
        MapScreenshot screenshot = new MapScreenshot(name, tag.getByteArray("screenshot"));
        int timerOverride = tag.getInt("timerOverride");
        int targetScoreOverride = tag.getInt("targetScoreOverride");
        return new ArenaMap(name, enabled, gamemode, corner1, corner2, time, raining, thundering, screenshot, timerOverride, targetScoreOverride);
    }

    public String getName() {
        return name;
    }

    public Component toComponent() {
        ArenaGamemode gamemode = getNewGamemode();
        return Component.literal(name).withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - from ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(corner1.toShortString()).withStyle(ChatFormatting.BLUE))
                .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(corner2.toShortString()).withStyle(ChatFormatting.BLUE))
                .append(Component.literal(", gamemode: ").withStyle(ChatFormatting.GRAY))
                .append((gamemode == null ? Component.literal("None") : gamemode.getName().copy()).withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("arena." + (enabled ? "enabled" : "disabled")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    public ArenaMapInfo getInfo(int votes) {
        ArenaGamemode gamemode = getNewGamemode();
        return new ArenaMapInfo(name, screenshot, gamemode == null ? EntropyArena.id("none") : gamemode.getRegistryID(), getSize(), votes);
    }

    public Vec3i getSize() {
        return corner2.subtract(corner1).offset(1, 1, 1);
    }

    public Vec3 getCenter() {
        return corner1.getCenter().lerp(corner2.getCenter(), 0.5);
    }

    public AABB getBoundingBox() {
        return AABB.encapsulatingFullBlocks(corner1, corner2);
    }

    public @Nullable Component validate(ServerLevel level) {
        ArenaGamemode gamemode = getNewGamemode();
        if (gamemode == null) return Component.translatable("arena.error.no_gamemode", gamemodeID.toString());
        return gamemode.validateMap(level, this);
    }

    public int getTimer() {
        return timerOverride > 0 ? timerOverride : ServerConfig.DEFAULT_ROUND_SECONDS.get();
    }

    public int getTargetScore() {
        return targetScoreOverride > 0 ? targetScoreOverride : ServerConfig.DEFAULT_TARGET_SCORE.get();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean newValue) {
        enabled = newValue;
    }
}
