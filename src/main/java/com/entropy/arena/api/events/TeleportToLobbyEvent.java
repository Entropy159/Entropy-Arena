package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class TeleportToLobbyEvent extends PlayerEvent {
    public TeleportToLobbyEvent(ServerPlayer player) {
        super(player);
    }

    public static class Pre extends TeleportToLobbyEvent {
        public Pre(ServerPlayer player) {
            super(player);
        }
    }

    public static class Post extends TeleportToLobbyEvent {
        public Post(ServerPlayer player) {
            super(player);
        }
    }
}
