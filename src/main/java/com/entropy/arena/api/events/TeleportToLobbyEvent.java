package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * See subclasses for more information.
 */
public abstract class TeleportToLobbyEvent extends PlayerEvent {
    public TeleportToLobbyEvent(ServerPlayer player) {
        super(player);
    }

    /**
     * Fired before all logic when a player is teleported to the lobby.
     */
    public static class Pre extends TeleportToLobbyEvent {
        public Pre(ServerPlayer player) {
            super(player);
        }
    }

    /**
     * Fired after all logic when a player is teleported to the lobby.
     */
    public static class Post extends TeleportToLobbyEvent {
        public Post(ServerPlayer player) {
            super(player);
        }
    }
}
