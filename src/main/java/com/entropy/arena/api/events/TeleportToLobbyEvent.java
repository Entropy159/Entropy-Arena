package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;
import net.minecraft.server.level.ServerPlayer;

public abstract class TeleportToLobbyEvent extends ArenaPlayerEvent {
    public TeleportToLobbyEvent(ArenaData data, ServerPlayer player) {
        super(data, player);
    }

    public static class Pre extends TeleportToLobbyEvent {
        public Pre(ArenaData data, ServerPlayer player) {
            super(data, player);
        }
    }

    public static class Post extends TeleportToLobbyEvent {
        public Post(ArenaData data, ServerPlayer player) {
            super(data, player);
        }
    }
}
