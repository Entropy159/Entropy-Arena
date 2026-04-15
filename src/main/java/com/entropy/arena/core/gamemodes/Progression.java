package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public class Progression extends FFAGamemode {
    public Progression() {
        super(EntropyArena.id("progression"), "Progression");
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer && player.getUUID() != killer.getUUID()) {
            incrementScore(killer);
            setScore(player, getScore(player) - 1);
            ArenaLogic.get(player.serverLevel()).giveStarterGear(player);
        }
        return super.onDeath(player, source);
    }

    @Override
    public void incrementScore(ServerPlayer player) {
        super.incrementScore(player);
        ArenaLogic.get(player.serverLevel()).giveStarterGear(player);
    }

    @Override
    public ItemStack getItemFromList(ServerPlayer player, ItemList list) {
        return list.get(Math.min(getScore(player), list.size() - 1));
    }

    @Override
    public boolean isValidLoadout(ServerLevel level, Loadout loadout) {
        return super.isValidLoadout(level, loadout) && !loadout.getItemLists(level).isEmpty();
    }

    @Override
    public boolean isValidItemList(ItemList list) {
        return !list.isRandom();
    }
}
