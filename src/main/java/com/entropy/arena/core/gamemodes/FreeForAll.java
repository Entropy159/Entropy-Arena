package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.data.ArenaLogic;
import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class FreeForAll extends FFAGamemode {
    public FreeForAll() {
        super(EntropyArena.id("free_for_all"));
    }

    @Override
    public void generateLang() {
        EntropyArena.REGISTRATE.addRawLang("arena.gamemode.entropyarena.free_for_all", "Free For All");
    }

    @Override
    public boolean onDeath(ArenaLogic data, ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer) {
            incrementScore(killer);
        }
        return super.onDeath(data, player, source);
    }
}
