package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;

public abstract class MatchEndEvent extends ArenaEvent {
    public MatchEndEvent(ArenaData data) {
        super(data);
    }

    public static class Pre extends MatchEndEvent {
        public Pre(ArenaData data) {
            super(data);
        }
    }

    public static class Post extends MatchEndEvent {
        public Post(ArenaData data) {
            super(data);
        }
    }
}
