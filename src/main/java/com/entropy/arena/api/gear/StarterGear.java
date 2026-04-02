package com.entropy.arena.api.gear;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.blocks.TeamBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;

import java.util.HashMap;

public class StarterGear {
    private final ServerPlayer player;
    private final HashMap<Integer, ItemStack> gear;

    public StarterGear(ServerPlayer player, ArenaTeam teamForBlock) {
        this.player = player;
        gear = new HashMap<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            gear.put(slot, ItemStack.EMPTY);
        }
        addDefaultItems(teamForBlock);
    }

    private void addDefaultItems(ArenaTeam teamForBlock) {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        gear.put(0, sword);
        ItemStack block = TeamBlock.getStack(teamForBlock);
        gear.put(8, block);
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public HashMap<Integer, ItemStack> getCurrentGear() {
        //noinspection unchecked
        return (HashMap<Integer, ItemStack>) gear.clone();
    }

    public ItemStack setItem(ItemStack stack, int slot) {
        if (slot < player.getInventory().getContainerSize()) {
            return gear.put(slot, stack);
        }
        return ItemStack.EMPTY;
    }

    public boolean addItem(ItemStack stack) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (getItem(slot).isEmpty()) {
                setItem(stack, slot);
                return true;
            }
        }
        return false;
    }

    public ItemStack getItem(int slot) {
        return gear.get(slot);
    }

    public void give() {
        gear.forEach((slot, stack) -> player.getInventory().setItem(slot, stack));
    }
}
