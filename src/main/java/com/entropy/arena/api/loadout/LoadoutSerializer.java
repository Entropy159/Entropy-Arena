package com.entropy.arena.api.loadout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class LoadoutSerializer {
    public abstract CompoundTag serialize(ServerPlayer player);

    public abstract void deserialize(ServerPlayer player, CompoundTag tag);

    public abstract List<ItemStack> getStacks(ServerPlayer player);

    public abstract void setStack(ServerPlayer player, int slot, ItemStack stack);

    public abstract void forEachStack(ServerPlayer player, BiConsumer<Integer, ItemStack> consumer);

    public boolean contains(ServerLevel level, CompoundTag data, Predicate<ItemStack> filter) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        deserialize(player, data);
        AtomicBoolean has = new AtomicBoolean(false);
        forEachStack(player, (slot, stack) -> has.set(has.get() || filter.test(stack)));
        return has.get();
    }

    public void clear(ServerPlayer player) {
        forEachStack(player, (slot, stack) -> setStack(player, slot, ItemStack.EMPTY));
    }
}
