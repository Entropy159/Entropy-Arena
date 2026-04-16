package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/**
 * An abstract parent class for an event with a ServerLevel. Cannot be used directly, create a subclass instead.
 */
public abstract class ServerLevelEvent extends Event {
    private final ServerLevel level;

    public ServerLevelEvent(ServerLevel level) {
        this.level = level;
    }

    public ServerLevel getLevel() {
        return level;
    }
}
