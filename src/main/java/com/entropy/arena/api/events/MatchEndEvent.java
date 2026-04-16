package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;

/**
 * See subclasses for more information.
 */
public abstract class MatchEndEvent extends ServerLevelEvent {
    public MatchEndEvent(ServerLevel level) {
        super(level);
    }

    /**
     * Fired before all logic when a match ends.
     */
    public static class Pre extends MatchEndEvent {
        public Pre(ServerLevel level) {
            super(level);
        }
    }

    /**
     * Fired after all logic when a match ends.
     */
    public static class Post extends MatchEndEvent {
        public Post(ServerLevel level) {
            super(level);
        }
    }
}
