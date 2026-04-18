package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.EntropyArena;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public class Progression extends FFAGamemode {
    @Override
    public void generateLang() {
        setNameTranslation("Progression");
    }

    @Override
    public ResourceLocation getRegistryID() {
        return EntropyArena.id("progression");
    }

    @Override
    public Registrate getRegistrate() {
        return EntropyArena.REGISTRATE;
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
    public boolean isValidLoadout(ServerPlayer player, Loadout loadout) {
        return super.isValidLoadout(player, loadout) && !loadout.getItemLists(player.serverLevel()).isEmpty();
    }

    @Override
    public boolean isValidItemList(ItemList list) {
        return !list.isRandom();
    }
}
