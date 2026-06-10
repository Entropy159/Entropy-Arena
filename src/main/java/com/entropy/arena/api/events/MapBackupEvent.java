package com.entropy.arena.api.events;

import com.entropy.arena.api.map.ArenaMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * These events fire during map backups. Use one of the subclasses.
 */
public abstract class MapBackupEvent extends ServerLevelEvent {
    public final ArenaMap map;

    public MapBackupEvent(ServerLevel level, ArenaMap map) {
        super(level);
        this.map = map;
    }

    public static class Start extends MapBackupEvent {
        public Start(ServerLevel level, ArenaMap map) {
            super(level, map);
        }
    }

    public static class ProcessChunk extends MapBackupEvent {
        public final ChunkPos chunk;

        public ProcessChunk(ServerLevel level, ArenaMap map, ChunkPos chunk) {
            super(level, map);
            this.chunk = chunk;
        }
    }

    public static class Finish extends MapBackupEvent {
        public Finish(ServerLevel level, ArenaMap map) {
            super(level, map);
        }
    }
}
