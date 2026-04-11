package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class TeamDeathmatch extends TeamGamemode {
    public TeamDeathmatch() {
        super(EntropyArena.id("team_deathmatch"), "Team Deathmatch");
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer) {
            incrementScore(getPlayerTeam(killer));
        }
        return super.onDeath(player, source);
    }
}
