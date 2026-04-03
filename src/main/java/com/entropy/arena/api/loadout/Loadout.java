package com.entropy.arena.api.loadout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class Loadout {
    private final CompoundTag gear;

    public Loadout(CompoundTag tag) {
        gear = tag;
    }

    public Loadout(ServerPlayer player) {
        gear = LoadoutSerializerRegistry.serializeWithAll(player);
    }

    public void giveToPlayer(ServerPlayer player) {
        LoadoutSerializerRegistry.deserializeWithAll(player, gear);
    }

    public CompoundTag getCompound() {
        return gear;
    }
}
