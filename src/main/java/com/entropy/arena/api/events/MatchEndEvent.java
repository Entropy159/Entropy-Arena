package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;

public abstract class MatchEndEvent extends ArenaEvent {
    public MatchEndEvent(ServerLevel level) {
        super(level);
    }

    public static class Pre extends MatchEndEvent {
        public Pre(ServerLevel level) {
            super(level);
        }
    }

    public static class Post extends MatchEndEvent {
        public Post(ServerLevel level) {
            super(level);
        }
    }
}
