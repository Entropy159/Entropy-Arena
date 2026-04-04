package com.entropy.arena.api.events;

import com.entropy.arena.api.loadout.Loadout;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class GiveLoadoutEvent extends PlayerEvent {
    private final Loadout loadout;

    public GiveLoadoutEvent(ServerPlayer player, Loadout loadout) {
        super(player);
        this.loadout = loadout;
    }

    public Loadout getLoadout() {
        return loadout;
    }
}
