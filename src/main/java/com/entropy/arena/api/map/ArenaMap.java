package com.entropy.arena.api.map;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.EventScheduler;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArenaMap {
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
    private static Backup backup;
    private final HashMap<Property<?>, HashMap<Object, ArrayList<BlockPos>>> blockPropertyMap = new HashMap<>();

    public ArenaMap(ServerLevel level, String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2) {
        this(name, gamemodeID, BlockPos.min(corner1, corner2), BlockPos.max(corner1, corner2), level.getDayTime(), level.isRaining(), level.isThundering(), new MapScreenshot(name), 0, 0);
    }

    public ArenaMap(String name, ResourceLocation gamemodeID, BlockPos corner1, BlockPos corner2, long time, boolean raining, boolean thundering, MapScreenshot screenshot, int timerOverride, int targetScoreOverride) {
        this.name = name;
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
        if (backup == null) {
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
        return GamemodeRegistry.getGamemode(gamemodeID);
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

    public void backup(ServerLevel level, List<Property<?>> propertiesToLookFor, Runnable after) {
        backup = new Backup(level, this, propertiesToLookFor, after);
    }

    public void forEachChunk(Consumer<ChunkPos> consumer) {
        for (int x = SectionPos.blockToSectionCoord(corner1.getX()); x <= SectionPos.blockToSectionCoord(corner2.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(corner1.getZ()); z <= SectionPos.blockToSectionCoord(corner2.getZ()); z++) {
                consumer.accept(new ChunkPos(x, z));
            }
        }
    }

    public void reset(ServerLevel level, Runnable after) {
        if (backup == null) {
            after.run();
            return;
        }
        backup.restore(level, after);
        backup = null;
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
        ResourceLocation gamemode = ResourceLocation.tryParse(tag.getString("gamemode"));
        BlockPos corner1 = BlockPos.of(tag.getLong("corner1"));
        BlockPos corner2 = BlockPos.of(tag.getLong("corner2"));
        long time = tag.getLong("time");
        boolean raining = tag.getBoolean("raining");
        boolean thundering = tag.getBoolean("thundering");
        MapScreenshot screenshot = new MapScreenshot(name, tag.getByteArray("screenshot"));
        int timerOverride = tag.getInt("timerOverride");
        int targetScoreOverride = tag.getInt("targetScoreOverride");
        return new ArenaMap(name, gamemode, corner1, corner2, time, raining, thundering, screenshot, timerOverride, targetScoreOverride);
    }

    public String getName() {
        return name;
    }

    public Component toComponent() {
        ArenaGamemode gamemode = getNewGamemode();
        return Component.literal(name).withStyle(ChatFormatting.YELLOW).append(Component.literal(" - from ").withStyle(ChatFormatting.GRAY)).append(Component.literal(corner1.toShortString()).withStyle(ChatFormatting.BLUE)).append(Component.literal(" to ").withStyle(ChatFormatting.GRAY)).append(Component.literal(corner2.toShortString()).withStyle(ChatFormatting.BLUE)).append(Component.literal(", gamemode: ").withStyle(ChatFormatting.GRAY)).append((gamemode == null ? Component.literal("None") : gamemode.getName().copy()).withStyle(ChatFormatting.DARK_AQUA));
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

    private static class Backup {
        private final File backupFolder;
        private final BlockPos corner1;
        private final BlockPos corner2;

        public Backup(ServerLevel level, ArenaMap map, List<Property<?>> propertiesToLookFor, Runnable after) {
            backupFolder = level.getServer().getFile("arena_backup").toFile();
            if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                EntropyArena.LOGGER.error("Failed to create map backup folder!");
            }
            corner1 = map.corner1;
            corner2 = map.corner2;
            Queue<ChunkPos> queue = new ArrayDeque<>();
            Set<ChunkPos> active = new HashSet<>();
            map.forEachChunk(queue::add);
            int totalChunks = queue.size();
            AtomicInteger currentChunk = new AtomicInteger(1);
            EventScheduler.scheduleUntil(1, queue::isEmpty, () -> {
                while (active.size() < ServerConfig.CONCURRENT_CHUNK_LOADS.get() && !queue.isEmpty()) {
                    ChunkPos pos = queue.poll();
                    if (pos == null) return;
                    active.add(pos);
                    level.setChunkForced(pos.x, pos.z, true);
                    EventScheduler.schedule(1, () -> level.areEntitiesLoaded(pos.toLong()), () -> {
                        forEachBlockInChunk(pos, p -> {
                            BlockState state = level.getBlockState(p);
                            for (Property<?> property : propertiesToLookFor) {
                                if (state.hasProperty(property)) {
                                    map.blockPropertyMap.computeIfAbsent(property, v -> new HashMap<>()).computeIfAbsent(state.getValue(property), v -> new ArrayList<>()).add(p);
                                }
                            }
                        });
                        StructureTemplate template = new StructureTemplate();
                        template.fillFromWorld(level, clampMinBlockPos(pos), getChunkAreaSize(pos), true, null);
                        try {
                            NbtIo.writeCompressed(template.save(new CompoundTag()), backupFolder.toPath().resolve(pos.toLong() + ".nbt"));
                        } catch (IOException e) {
                            EntropyArena.LOGGER.error("Error saving map backup", e);
                        }
                        level.setChunkForced(pos.x, pos.z, false);
                        active.remove(pos);
                        int chunkIndex = currentChunk.getAndIncrement();
                        level.players().forEach(player -> player.displayClientMessage(Component.translatable("arena.message.chunk_load_progress", chunkIndex, totalChunks), true));
                        if (queue.isEmpty() && active.isEmpty()) {
                            after.run();
                        }
                    });
                }
            });
        }

        private void forEachBlockInChunk(ChunkPos pos, Consumer<BlockPos> function) {
            BlockPos min = clampMinBlockPos(pos);
            BlockPos max = clampMaxBlockPos(pos);
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        function.accept(new BlockPos(x, y, z));
                    }
                }
            }
        }

        public void restore(ServerLevel level, Runnable after) {
            Queue<ChunkPos> queue = new ArrayDeque<>();
            Set<ChunkPos> active = new HashSet<>();
            File[] dataFiles = backupFolder.listFiles(file -> file.getName().endsWith(".nbt"));
            if (dataFiles != null) {
                for (File dataFile : dataFiles) {
                    queue.add(new ChunkPos(Long.parseLong(dataFile.getName().replace(".nbt", ""))));
                }
            }
            int totalChunks = queue.size();
            AtomicInteger currentChunk = new AtomicInteger(1);
            EventScheduler.scheduleUntil(1, queue::isEmpty, () -> {
                while (active.size() < ServerConfig.CONCURRENT_CHUNK_LOADS.get() && !queue.isEmpty()) {
                    ChunkPos pos = queue.poll();
                    if (pos == null) return;
                    active.add(pos);
                    level.setChunkForced(pos.x, pos.z, true);
                    EventScheduler.schedule(1, () -> level.areEntitiesLoaded(pos.toLong()), () -> {
                        level.getEntities((Entity) null, new AABB(clampMinBlockPos(pos).getCenter().subtract(0.5, 0.5, 0.5), clampMaxBlockPos(pos).getCenter().add(0.5, 0.5, 0.5)), e -> !(e instanceof Player)).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
                        try {
                            CompoundTag tag = NbtIo.readCompressed(backupFolder.toPath().resolve(pos.toLong() + ".nbt"), NbtAccounter.unlimitedHeap());
                            StructureTemplate template = new StructureTemplate();
                            template.load(level.holderLookup(Registries.BLOCK), tag);
                            template.placeInWorld(level, clampMinBlockPos(pos), clampMinBlockPos(pos), new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
                        } catch (IOException e) {
                            EntropyArena.LOGGER.error("Error reading map backup", e);
                        }
                        level.setChunkForced(pos.x, pos.z, false);
                        active.remove(pos);
                        int chunkIndex = currentChunk.getAndIncrement();
                        level.players().forEach(player -> player.displayClientMessage(Component.translatable("arena.message.chunk_reset_progress", chunkIndex, totalChunks), true));
                        if (!backupFolder.toPath().resolve(pos.toLong() + ".nbt").toFile().delete()) {
                            EntropyArena.LOGGER.error("Failed to delete map backup file {}!", pos);
                        }
                        if (queue.isEmpty() && active.isEmpty()) {
                            after.run();
                        }
                    });
                }
            });
        }

        private BlockPos clampMinBlockPos(ChunkPos pos) {
            return BlockPos.max(corner1, new BlockPos(pos.getMinBlockX(), corner1.getY(), pos.getMinBlockZ()));
        }

        private BlockPos clampMaxBlockPos(ChunkPos pos) {
            return BlockPos.min(corner2, new BlockPos(pos.getMaxBlockX(), corner2.getY(), pos.getMaxBlockZ()));
        }

        private Vec3i getChunkAreaSize(ChunkPos pos) {
            return clampMaxBlockPos(pos).subtract(clampMinBlockPos(pos)).offset(1, 1, 1);
        }
    }
}
