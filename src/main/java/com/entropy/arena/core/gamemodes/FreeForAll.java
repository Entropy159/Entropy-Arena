package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.FFAGamemode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class FreeForAll extends FFAGamemode {
    public FreeForAll(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer && player.getUUID() != killer.getUUID()) {
            incrementScore(killer);
        }
        return super.onDeath(player, source);
    }
}
