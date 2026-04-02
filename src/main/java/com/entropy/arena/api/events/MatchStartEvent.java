package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;

public abstract class MatchStartEvent extends ArenaEvent {
    public MatchStartEvent(ArenaData data) {
        super(data);
    }

    public static class Pre extends MatchStartEvent {
        public Pre(ArenaData data) {
            super(data);
        }
    }

    public static class Post extends MatchStartEvent {
        public Post(ArenaData data) {
            super(data);
        }
    }
}
