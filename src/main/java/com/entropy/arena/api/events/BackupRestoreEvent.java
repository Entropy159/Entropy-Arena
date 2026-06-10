package com.entropy.arena.api.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * These events fire when backups are restored. Use one of the subclasses.
 */
public abstract class BackupRestoreEvent extends ServerLevelEvent {
    public BackupRestoreEvent(ServerLevel level) {
        super(level);
    }

    public static class Start extends BackupRestoreEvent {
        public Start(ServerLevel level) {
            super(level);
        }
    }

    public static class ProcessChunk extends BackupRestoreEvent {
        public final ChunkPos chunk;
        public final CompoundTag data;

        public ProcessChunk(ServerLevel level, ChunkPos chunk, CompoundTag data) {
            super(level);
            this.chunk = chunk;
            this.data = data;
        }
    }

    public static class Finish extends BackupRestoreEvent {
        public Finish(ServerLevel level) {
            super(level);
        }
    }
}
