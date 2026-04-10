package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;

/**
 * See subclasses for more information.
 */
public abstract class MatchStartEvent extends ArenaEvent {
    public MatchStartEvent(ServerLevel level) {
        super(level);
    }

    /**
     * Fired before all logic when a match starts.
     */
    public static class Pre extends MatchStartEvent {
        public Pre(ServerLevel level) {
            super(level);
        }
    }

    /**
     * Fired after all logic when a match starts.
     */
    public static class Post extends MatchStartEvent {
        public Post(ServerLevel level) {
            super(level);
        }
    }
}
