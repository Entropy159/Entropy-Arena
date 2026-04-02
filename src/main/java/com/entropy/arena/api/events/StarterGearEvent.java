package com.entropy.arena.api.events;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gear.StarterGear;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class StarterGearEvent extends Event {
    private final ArenaGamemode gamemode;
    private final ServerPlayer player;
    private final StarterGear gear;

    public StarterGearEvent(ArenaGamemode gamemode, ServerPlayer player, StarterGear gear) {
        this.gamemode = gamemode;
        this.player = player;
        this.gear = gear;
    }

    public ArenaGamemode getGamemode() {
        return gamemode;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public StarterGear getGear() {
        return gear;
    }
}
