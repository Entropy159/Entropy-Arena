package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.TeamGamemode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class TeamDeathmatch extends TeamGamemode {
    public TeamDeathmatch(ResourceLocation id) {
        super(id);
    }

    @Override
    public void onDeath(ServerPlayer player, DamageSource source) {
        super.onDeath(player, source);
        if (source.getEntity() instanceof ServerPlayer killer && getPlayerTeam(player) != getPlayerTeam(killer)) {
            incrementScore(getPlayerTeam(killer));
        }
    }
}
