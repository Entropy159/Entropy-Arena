package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;

public abstract class MatchStartEvent extends ArenaEvent {
    public MatchStartEvent(ServerLevel level) {
        super(level);
    }

    public static class Pre extends MatchStartEvent {
        public Pre(ServerLevel level) {
            super(level);
        }
    }

    public static class Post extends MatchStartEvent {
        public Post(ServerLevel level) {
            super(level);
        }
    }
}
