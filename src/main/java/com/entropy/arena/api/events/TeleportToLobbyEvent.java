package com.entropy.arena.api.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class TeleportToLobbyEvent extends PlayerEvent {
    public TeleportToLobbyEvent(Player player) {
        super(player);
    }
}
