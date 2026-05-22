package com.entropy.arena.api.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class KillStreakEvent extends PlayerEvent {
    private final int oldValue;
    private final int newValue;

    public KillStreakEvent(ServerPlayer player, int oldValue, int newValue) {
        super(player);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }

    @Override
    public @NotNull ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }
}
