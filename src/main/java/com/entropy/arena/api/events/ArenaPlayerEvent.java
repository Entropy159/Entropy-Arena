package com.entropy.arena.api.events;

import com.entropy.arena.api.data.ArenaData;
import net.minecraft.server.level.ServerPlayer;

public abstract class ArenaPlayerEvent extends ArenaEvent {
    private final ServerPlayer player;

    public ArenaPlayerEvent(ArenaData data, ServerPlayer player) {
        super(data);
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
