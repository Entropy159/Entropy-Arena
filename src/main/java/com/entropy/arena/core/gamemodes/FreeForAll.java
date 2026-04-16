package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.core.EntropyArena;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class FreeForAll extends FFAGamemode {
    @Override
    public void generateLang() {
        setNameTranslation("Free for All");
    }

    @Override
    public ResourceLocation getRegistryID() {
        return EntropyArena.id("free_for_all");
    }

    @Override
    public Registrate getRegistrate() {
        return EntropyArena.REGISTRATE;
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer && player.getUUID() != killer.getUUID()) {
            incrementScore(killer);
        }
        return super.onDeath(player, source);
    }
}
