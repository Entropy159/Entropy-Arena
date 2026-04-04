package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public abstract class ArenaEvent extends Event {
    private final ServerLevel level;

    public ArenaEvent(ServerLevel level) {
        this.level = level;
    }

    public ServerLevel getLevel() {
        return level;
    }
}
