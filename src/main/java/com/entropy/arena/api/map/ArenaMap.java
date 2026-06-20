package com.entropy.arena.api.map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.StringUtils;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.api.util.ArenaTeam;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.SpawnpointBlock;
import com.entropy.arena.core.network.toClient.ConfigOverridesPacket;
import com.entropy.arena.core.network.toClient.TakeScreenshotPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArenaMap {
    private boolean enabled;
    private final String name;
    private final ResourceKey<Level> dimension;
    private final ResourceLocation gamemodeID;
    private final BlockPos corner1;
    private final BlockPos corner2;
    private long time;
    private boolean raining;
    private boolean thundering;
    private MapScreenshot screenshot;
    protected final HashMap<Property<?>, HashMap<Object, ArrayList<BlockPos>>> blockPropertyMap = new HashMap<>();
    private final Map<String, CommentedConfig> configOverrides;

    public ArenaMap(ServerLevel level, String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2) {
        this(name, true, level.dimension(), gamemodeID, BlockPos.min(corner1, corner2), BlockPos.max(corner1, corner2), level.getDayTime(), level.isRaining(), level.isThundering(), new MapScreenshot(name), new HashMap<>());
    }

    public ArenaMap(String name, boolean enabled, ResourceKey<Level> dimension, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2, long time, boolean raining, boolean thundering, MapScreenshot screenshot, Map<String, CommentedConfig> configOverrides) {
        this.name = name;
        this.enabled = enabled;
        this.dimension = dimension;
        this.gamemodeID = gamemodeID;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.time = time;
        this.raining = raining;
        this.thundering = thundering;
        this.screenshot = screenshot;
        this.configOverrides = configOverrides;
    }

    public @Nullable ServerLevel getLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : getLevel(server);
    }

    public @Nullable ServerLevel getLevel(MinecraftServer server) {
        return server.getLevel(dimension);
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

    public void setScreenshot(MapScreenshot newScreenshot) {
        screenshot = newScreenshot;
    }

    public void load(ServerLevel level) {
        level.setWeatherParameters(99999, 99999, raining, thundering);
        level.setDayTime(time);
    }

    public void setWorldBorder(ServerLevel level) {
        level.getWorldBorder().setCenter(getCenter().x, getCenter().y);
        level.getWorldBorder().setSize(Math.max(getSize().getX(), getSize().getZ()));
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

    public <T> @Nullable T getConfigValue(List<String> path, String modID) {
        CommentedConfig commentedConfig = configOverrides.get(modID);
        if (commentedConfig == null) {
            return null;
        }
        T value = commentedConfig.get(path);
        return value == null || value instanceof Config ? null : value;
    }

    public <T> void setConfigOverride(ModConfigSpec.ConfigValue<T> config, T value, String modID) {
        setConfigOverride(config.getPath(), value, modID);
    }

    public <T> void setConfigOverride(String path, T value, String modID) {
        setConfigOverride(StringUtils.split(path, '.'), value, modID);
    }

    public <T> void setConfigOverride(List<String> path, T value, String modID) {
        configOverrides.computeIfAbsent(modID, tuple -> TomlFormat.newConfig()).set(path, value);
    }

    public void resetConfigOverride(String path, String modID) {
        CommentedConfig config = configOverrides.get(modID);
        if (config != null) {
            config.remove(path);
        }
    }

    public boolean hasConfigOverride(String modID, String key) {
        return configOverrides.entrySet().stream().anyMatch(entry -> Objects.equals(entry.getKey(), modID) && entry.getValue().contains(key) && !(entry.getValue().get(key) instanceof Config));
    }

    public void syncConfig(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, new ConfigOverridesPacket(configOverrides));
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putBoolean("enabled", enabled);
        tag.putString("dimension", dimension.location().toString());
        tag.putString("gamemode", gamemodeID.toString());
        tag.putLong("corner1", corner1.asLong());
        tag.putLong("corner2", corner2.asLong());
        tag.putLong("time", time);
        tag.putBoolean("raining", raining);
        tag.putBoolean("thundering", thundering);
        tag.putByteArray("screenshot", screenshot.getData());
        CompoundTag configs = new CompoundTag();
        configOverrides.forEach((modID, config) -> {
            String configString = new TomlWriter().writeToString(config);
            configs.putString(modID, configString);
        });
        tag.put("configOverrides", configs);
        return tag;
    }

    public static ArenaMap fromTag(CompoundTag tag) {
        String name = tag.getString("name");
        boolean enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        ResourceKey<Level> dimension = Level.OVERWORLD;
        if (tag.contains("dimension")) {
            dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")));
        }
        ResourceLocation gamemode = ResourceLocation.tryParse(tag.getString("gamemode"));
        BlockPos corner1 = BlockPos.of(tag.getLong("corner1"));
        BlockPos corner2 = BlockPos.of(tag.getLong("corner2"));
        long time = tag.getLong("time");
        boolean raining = tag.getBoolean("raining");
        boolean thundering = tag.getBoolean("thundering");
        MapScreenshot screenshot = new MapScreenshot(name, tag.getByteArray("screenshot"));
        Map<String, CommentedConfig> configOverrides = new HashMap<>();
        if (tag.contains("configOverrides")) {
            CompoundTag configs = tag.getCompound("configOverrides");
            for (String key : configs.getAllKeys()) {
                configOverrides.put(key, new TomlParser().parse(configs.getString(key)));
            }
        }
        return new ArenaMap(name, enabled, dimension, gamemode, corner1, corner2, time, raining, thundering, screenshot, configOverrides);
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
                .append(Component.literal(", dimension: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(dimension.location().toString()))
                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("arena." + (enabled ? "enabled" : "disabled")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.literal(", config overrides: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(configOverrides.values().stream().map(config -> config.entrySet().stream().filter(entry -> !(entry.getValue() instanceof Config)).toList().size()).reduce(0, Integer::sum).toString()).withStyle(ChatFormatting.DARK_PURPLE));
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

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isValid() {
        return isEnabled() && getLevel() != null;
    }

    public void setEnabled(boolean newValue) {
        enabled = newValue;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}
