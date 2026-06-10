package com.entropy.arena.api.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;

/**
 * See subclasses for more information.
 */
public abstract class MatchStartEvent extends ServerLifecycleEvent {
    public MatchStartEvent(MinecraftServer server) {
        super(server);
    }

    /**
     * Fired before all logic when a match starts.
     */
    public static class Pre extends MatchStartEvent {
        public Pre(MinecraftServer server) {
            super(server);
        }
    }

    /**
     * Fired after all logic when a match starts.
     */
    public static class Post extends MatchStartEvent {
        public final ServerLevel level;

        public Post(ServerLevel level) {
            super(level.getServer());
            this.level = level;
        }
    }
}
