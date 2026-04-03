package com.entropy.arena.core.loadout;

import com.entropy.arena.api.loadout.LoadoutSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class VanillaLoadoutSerializer extends LoadoutSerializer {
    @Override
    public CompoundTag serialize(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", player.getInventory().save(new ListTag()));
        return tag;
    }

    @Override
    public void deserialize(ServerPlayer player, CompoundTag tag) {
        ListTag inventory = tag.getList("inventory", 10);
        player.getInventory().load(inventory);
    }

    @Override
    public List<ItemStack> getStacks(ServerPlayer player) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        forEachStack(player, (slot, stack) -> {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        });
        return stacks;
    }

    @Override
    public void setStack(ServerPlayer player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    @Override
    public void forEachStack(ServerPlayer player, BiConsumer<Integer, ItemStack> consumer) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            consumer.accept(slot, player.getInventory().getItem(slot));
        }
    }
}
