package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.loadout.Loadout;
import net.minecraft.server.level.ServerPlayer;

public class GiveLoadoutEvent extends ArenaPlayerEvent {
    private final Loadout loadout;

    public GiveLoadoutEvent(ArenaData data, ServerPlayer player, Loadout loadout) {
        super(data, player);
        this.loadout = loadout;
    }

    public Loadout getLoadout() {
        return loadout;
    }
}
