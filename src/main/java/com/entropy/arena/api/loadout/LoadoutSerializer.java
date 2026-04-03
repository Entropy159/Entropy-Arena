package com.entropy.arena.api.loadout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class LoadoutSerializer {
    public abstract CompoundTag serialize(ServerPlayer player);

    public abstract void deserialize(ServerPlayer player, CompoundTag tag);

    public abstract List<ItemStack> getStacks(ServerPlayer player);

    public abstract void setStack(ServerPlayer player, int slot, ItemStack stack);

    public abstract void forEachStack(ServerPlayer player, BiConsumer<Integer, ItemStack> consumer);
}
