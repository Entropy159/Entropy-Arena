package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class FreeForAll extends FFAGamemode {
    public FreeForAll() {
        super(EntropyArena.id("free_for_all"), "Free for All");
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer && player.getUUID() != killer.getUUID()) {
            incrementScore(killer);
        }
        return super.onDeath(player, source);
    }
}
