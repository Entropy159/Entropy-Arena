package com.entropy.arena.api.events;

import com.entropy.arena.api.loadout.Loadout;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a loadout is given to a player.
 */
public class GiveLoadoutEvent extends PlayerEvent {
    private final Loadout loadout;

    public GiveLoadoutEvent(ServerPlayer player, Loadout loadout) {
        super(player);
        this.loadout = loadout;
    }

    @Override
    public @NotNull ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }

    public Loadout getLoadout() {
        return loadout;
    }
}
