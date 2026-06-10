package com.entropy.arena.api.map;

import com.entropy.arena.api.events.BackupRestoreEvent;
import com.entropy.arena.api.events.MapBackupEvent;
import com.entropy.arena.api.util.EventScheduler;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ArenaMapBackup {
    public static void backup(MinecraftServer server, ArenaMap map, List<Property<?>> properties, Runnable after) {
        EntropyArena.LOGGER.info("Backing up map {} in {}", map.getName(), map.getDimension().location());
        ServerLevel level = map.getLevel(server);
        if (level == null) {
            EntropyArena.LOGGER.error("Level for map {} is null! Cannot back up", map.getName());
            after.run();
            return;
        }
        BlockPos corner1 = BlockPos.containing(map.getBoundingBox().getMinPosition());
        BlockPos corner2 = BlockPos.containing(map.getBoundingBox().getMaxPosition());
        Queue<ChunkPos> queue = new ArrayDeque<>();
        Set<ChunkPos> active = new HashSet<>();
        map.forEachChunk(queue::add);
        int totalChunks = queue.size();
        AtomicInteger currentChunk = new AtomicInteger(1);
        NeoForge.EVENT_BUS.post(new MapBackupEvent.Start(level, map));
        EventScheduler.scheduleUntil(1, queue::isEmpty, () -> {
            while (active.size() < ServerConfig.CONCURRENT_CHUNK_LOADS.get() && !queue.isEmpty()) {
                ChunkPos pos = queue.poll();
                if (pos == null) return;
                active.add(pos);
                level.setChunkForced(pos.x, pos.z, true);
                EventScheduler.schedule(1, () -> level.areEntitiesLoaded(pos.toLong()), () -> {
                    NeoForge.EVENT_BUS.post(new MapBackupEvent.ProcessChunk(level, map, pos));
                    forEachBlockInChunk(corner1, corner2, pos, p -> {
                        BlockState state = level.getBlockState(p);
                        for (Property<?> property : properties) {
                            if (state.hasProperty(property)) {
                                map.blockPropertyMap.computeIfAbsent(property, v -> new HashMap<>()).computeIfAbsent(state.getValue(property), v -> new ArrayList<>()).add(p);
                            }
                        }
                    });
                    StructureTemplate template = new StructureTemplate();
                    template.fillFromWorld(level, clampMinBlockPos(corner1, pos), getChunkAreaSize(corner1, corner2, pos), true, null);
                    CompoundTag tag = new CompoundTag();
                    tag.put("data", template.save(new CompoundTag()));
                    tag.putLong("pos", clampMinBlockPos(corner1, pos).asLong());
                    try {
                        EntropyArena.LOGGER.info("Writing backup for {} to {}", level.dimension().location(), getBackupFolder(level).toPath());
                        NbtIo.writeCompressed(tag, getBackupFolder(level).toPath().resolve(pos.toLong() + ".nbt"));
                    } catch (IOException e) {
                        EntropyArena.LOGGER.error("Error saving map backup", e);
                    }
                    level.setChunkForced(pos.x, pos.z, false);
                    active.remove(pos);
                    int chunkIndex = currentChunk.getAndIncrement();
                    level.players().forEach(player -> player.displayClientMessage(Component.translatable("arena.message.chunk_load_progress", chunkIndex, totalChunks), true));
                    if (queue.isEmpty() && active.isEmpty()) {
                        NeoForge.EVENT_BUS.post(new MapBackupEvent.Finish(level, map));
                        after.run();
                    }
                });
            }
        });
    }

    private static void forEachBlockInChunk(BlockPos minPos, BlockPos maxPos, ChunkPos pos, Consumer<BlockPos> function) {
        BlockPos min = clampMinBlockPos(minPos, pos);
        BlockPos max = clampMaxBlockPos(maxPos, pos);
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    function.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    private static File getBackupFolder(ServerLevel level) {
        File backupFolder = level.getServer().getFile("arena_backup").resolve(level.dimension().location().toLanguageKey()).toFile();
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            EntropyArena.LOGGER.error("Failed to create map backup folder!");
        }
        return backupFolder;
    }

    private static BlockPos clampMinBlockPos(BlockPos min, ChunkPos pos) {
        return BlockPos.max(min, new BlockPos(pos.getMinBlockX(), min.getY(), pos.getMinBlockZ()));
    }

    private static BlockPos clampMaxBlockPos(BlockPos max, ChunkPos pos) {
        return BlockPos.min(max, new BlockPos(pos.getMaxBlockX(), max.getY(), pos.getMaxBlockZ()));
    }

    private static Vec3i getChunkAreaSize(BlockPos minPos, BlockPos maxPos, ChunkPos pos) {
        return clampMaxBlockPos(maxPos, pos).subtract(clampMinBlockPos(minPos, pos)).offset(1, 1, 1);
    }

    public static void restore(MinecraftServer server, Runnable after) {
        Set<GlobalPos> active = new HashSet<>();
        server.getAllLevels().forEach(level -> {
            Queue<ChunkPos> queue = new ArrayDeque<>();
            File[] dataFiles = getBackupFolder(level).listFiles(file -> file.getName().endsWith(".nbt"));
            if (dataFiles != null) {
                for (File dataFile : dataFiles) {
                    queue.add(new ChunkPos(Long.parseLong(dataFile.getName().replace(".nbt", ""))));
                }
            }
            int totalChunks = queue.size();
            AtomicInteger currentChunk = new AtomicInteger(1);
            if (queue.isEmpty()) {
                EntropyArena.LOGGER.warn("Tried to restore an empty backup for {}, ignoring", level.dimension().location());
                after.run();
                return;
            }
            NeoForge.EVENT_BUS.post(new BackupRestoreEvent.Start(level));
            EventScheduler.scheduleUntil(1, queue::isEmpty, () -> {
                while (active.size() < ServerConfig.CONCURRENT_CHUNK_LOADS.get() && !queue.isEmpty()) {
                    ChunkPos pos = queue.poll();
                    if (pos == null) return;
                    active.add(GlobalPos.of(level.dimension(), pos.getWorldPosition()));
                    level.setChunkForced(pos.x, pos.z, true);
                    EventScheduler.schedule(1, () -> level.areEntitiesLoaded(pos.toLong()), () -> {
                        try {
                            CompoundTag tag = NbtIo.readCompressed(getBackupFolder(level).toPath().resolve(pos.toLong() + ".nbt"), NbtAccounter.unlimitedHeap());
                            NeoForge.EVENT_BUS.post(new BackupRestoreEvent.ProcessChunk(level, pos, tag));
                            BlockPos minPos = BlockPos.of(tag.getLong("pos"));
                            StructureTemplate template = new StructureTemplate();
                            template.load(level.holderLookup(Registries.BLOCK), tag.getCompound("data"));
                            level.getEntities((Entity) null, new AABB(minPos.getCenter().subtract(0.5, 0.5, 0.5), minPos.offset(template.getSize()).getCenter().add(0.5, 0.5, 0.5)), e -> !(e instanceof Player)).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
                            template.placeInWorld(level, minPos, minPos, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
                        } catch (IOException e) {
                            EntropyArena.LOGGER.error("Error reading map backup", e);
                        }
                        level.setChunkForced(pos.x, pos.z, false);
                        active.remove(GlobalPos.of(level.dimension(), pos.getWorldPosition()));
                        int chunkIndex = currentChunk.getAndIncrement();
                        level.players().forEach(player -> player.displayClientMessage(Component.translatable("arena.message.chunk_reset_progress", chunkIndex, totalChunks), true));
                        if (!getBackupFolder(level).toPath().resolve(pos.toLong() + ".nbt").toFile().delete()) {
                            EntropyArena.LOGGER.error("Failed to delete map backup file {}!", pos);
                        }
                        if (queue.isEmpty() && active.isEmpty()) {
                            NeoForge.EVENT_BUS.post(new BackupRestoreEvent.Finish(level));
                            after.run();
                        }
                    });
                }
            });
        });
    }

    public enum BackupState {
        NO_BACKUP(false),
        BACKING_UP(true),
        RESTORING(true),
        HAS_BACKUP(false);

        public final boolean inProgress;

        BackupState(boolean progress) {
            inProgress = progress;
        }
    }
}
