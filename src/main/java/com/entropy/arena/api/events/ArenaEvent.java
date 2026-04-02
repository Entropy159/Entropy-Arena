package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;
import net.neoforged.bus.api.Event;

public abstract class ArenaEvent extends Event {
    private final ArenaData data;

    public ArenaEvent(ArenaData data) {
        this.data = data;
    }

    public ArenaData getData() {
        return data;
    }
}
