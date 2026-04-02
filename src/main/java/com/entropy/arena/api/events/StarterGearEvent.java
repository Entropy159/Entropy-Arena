package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gear.StarterGear;
import net.minecraft.server.level.ServerPlayer;

public class StarterGearEvent extends ArenaEvent {
    private final ServerPlayer player;
    private final StarterGear gear;

    public StarterGearEvent(ArenaData data, ServerPlayer player, StarterGear gear) {
        super(data);
        this.player = player;
        this.gear = gear;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public StarterGear getGear() {
        return gear;
    }
}
