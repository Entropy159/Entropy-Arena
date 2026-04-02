package com.entropy.arena.core.map;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.EventScheduler;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.SpawnpointBlock;
import com.entropy.arena.core.network.toClient.TakeScreenshotPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class ArenaMap {
    private final String name;
    private final ResourceLocation gamemodeID;
    private final BlockPos corner1;
    private final BlockPos corner2;
    private long time;
    private boolean raining;
    private boolean thundering;
    private MapScreenshot screenshot;
    private static CompoundTag backup;

    public ArenaMap(ServerLevel level, String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2) {
        this(name, gamemodeID, ArenaUtils.min(corner1, corner2), ArenaUtils.max(corner1, corner2), level.getDayTime(), level.isRaining(), level.isThundering(), new MapScreenshot(name));
    }

    public ArenaMap(String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2, long time, boolean raining, boolean thundering, MapScreenshot screenshot) {
        this.name = name;
        this.gamemodeID = gamemodeID;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.time = time;
        this.raining = raining;
        this.thundering = thundering;
        this.screenshot = screenshot;
    }

    public void forEachBlock(BiConsumer<Vec3i, BlockPos> function) {
        Vec3i size = corner2.subtract(corner1);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    function.accept(new Vec3i(x, y, z), corner1.offset(x, y, z));
                }
            }
        }
    }

    public ArrayList<ArenaTeam> getTeams(ServerLevel level) {
        return new ArrayList<>(getSpawns(level).keySet().stream().filter(t -> t != ArenaTeam.NONE).sorted(Comparator.comparingInt(Enum::ordinal)).toList());
    }

    public HashMap<ArenaTeam, ArrayList<BlockPos>> getSpawns(ServerLevel level) {
        return getBlockPropertyMap(level, SpawnpointBlock.SPAWN_COLOR);
    }

    public <T extends Comparable<T>> HashMap<T, ArrayList<BlockPos>> getBlockPropertyMap(ServerLevel level, Property<T> property) {
        HashMap<T, ArrayList<BlockPos>> map = new HashMap<>();
        forEachBlock((offset, pos) -> {
            if (level.getBlockState(pos).hasProperty(property)) {
                T value = level.getBlockState(pos).getValue(property);
                map.computeIfAbsent(value, v -> new ArrayList<>()).add(pos);
            }
        });
        return map;
    }

    public @Nullable ArenaGamemode getNewGamemode() {
        return GamemodeRegistry.getGamemode(gamemodeID);
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

    public void backup(ServerLevel level) {
        loadChunks(level);
        EntropyArena.LOGGER.info("Chunk loading finished at tick {}", level.getServer().getTickCount());
        EventScheduler.schedule(2, () -> {
            AtomicBoolean isLoaded = new AtomicBoolean();
            forEachChunk(level, (pos, chunk) -> isLoaded.set(isLoaded.get() || level.areEntitiesLoaded(pos.toLong())));
            EntropyArena.LOGGER.info("Is loaded at tick {}: {}", level.getServer().getTickCount(), isLoaded.get());
            StructureTemplate template = new StructureTemplate();
            template.fillFromWorld(level, corner1, corner2.subtract(corner1), true, null);
            backup = template.save(new CompoundTag());
            try {
                NbtIo.writeCompressed(backup, FMLPaths.GAMEDIR.get().resolve("backup.nbt"));
                EntropyArena.LOGGER.info("Wrote backup to backup.nbt");
            } catch (IOException e) {
                EntropyArena.LOGGER.error("Error writing backup file", e);
            }
            unloadChunks(level);
        });
    }

    private void loadChunks(ServerLevel level) {
        forEachChunk(level, (pos, chunk) -> {
            level.getChunkSource().addRegionTicket(TicketType.PLAYER, pos, 0, pos, true);
            level.getChunkSource().getChunkFuture(pos.x, pos.z, ChunkStatus.FULL, true).join();
        });
    }

    private void unloadChunks(ServerLevel level) {
        forEachChunk(level, (pos, chunk) -> level.getChunkSource().removeRegionTicket(TicketType.PLAYER, pos, 0, pos, true));
    }

    public void forEachChunk(ServerLevel level, BiConsumer<ChunkPos, LevelChunk> consumer) {
        for (int x = SectionPos.blockToSectionCoord(corner1.getX()); x <= SectionPos.blockToSectionCoord(corner2.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(corner1.getZ()); z <= SectionPos.blockToSectionCoord(corner2.getZ()); z++) {
                consumer.accept(new ChunkPos(x, z), level.getChunk(x, z));
            }
        }
    }

    public void reset(ServerLevel level) {
        if (backup == null) return;
        loadChunks(level);
        level.getEntities((Entity) null, new AABB(corner1.getCenter(), corner2.getCenter()).inflate(1), e -> !(e instanceof Player)).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
        StructureTemplate template = new StructureTemplate();
        template.load(level.holderLookup(Registries.BLOCK), backup);
        template.placeInWorld(level, corner1, corner1, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        unloadChunks(level);
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
        return tag;
    }

    public static ArenaMap fromTag(CompoundTag tag) {
        String name = tag.getString("name");
        ResourceLocation gamemode = ResourceLocation.tryParse(tag.getString("gamemode"));
        BlockPos corner1 = BlockPos.of(tag.getLong("corner1"));
        BlockPos corner2 = BlockPos.of(tag.getLong("corner2"));
        long time = tag.getLong("time");
        boolean raining = tag.getBoolean("raining");
        boolean thundering = tag.getBoolean("thundering");
        MapScreenshot screenshot = new MapScreenshot(name, tag.getByteArray("screenshot"));
        return new ArenaMap(name, gamemode, corner1, corner2, time, raining, thundering, screenshot);
    }

    public String getName() {
        return name;
    }

    public Component toComponent() {
        ArenaGamemode gamemode = getNewGamemode();
        return Component.literal(name).withStyle(ChatFormatting.YELLOW).append(Component.literal(" - from ").withStyle(ChatFormatting.GRAY)).append(Component.literal(corner1.toShortString()).withStyle(ChatFormatting.BLUE)).append(Component.literal(" to ").withStyle(ChatFormatting.GRAY)).append(Component.literal(corner2.toShortString()).withStyle(ChatFormatting.BLUE)).append(Component.literal(", gamemode: ").withStyle(ChatFormatting.GRAY)).append((gamemode == null ? Component.literal("None") : gamemode.getName().copy()).withStyle(ChatFormatting.DARK_AQUA)).append(Component.literal(", ").withStyle(ChatFormatting.GRAY)).append(screenshot.isPresent() ? Component.literal("has screenshot").withStyle(ChatFormatting.GREEN) : Component.literal("no screenshot").withStyle(ChatFormatting.RED));
    }

    public ArenaMapInfo getInfo(ServerLevel level) {
        ArenaGamemode gamemode = getNewGamemode();
        return new ArenaMapInfo(name, screenshot, gamemode == null ? EntropyArena.id("none") : gamemode.getRegistryID(), getSpawns(level).size());
    }

    public Vec3 getCenter() {
        return corner1.getCenter().lerp(corner2.getCenter(), 0.5);
    }

    public AABB getBoundingBox() {
        return AABB.encapsulatingFullBlocks(corner1, corner2);
    }
}
