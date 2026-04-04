package com.entropy.arena.core.loadout;

import com.entropy.arena.api.loadout.LoadoutSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CuriosLoadoutSerializer extends LoadoutSerializer {
    @Override
    public CompoundTag serialize(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((name, curio) -> tag.put(name, curio.serializeNBT())));
        return tag;
    }

    @Override
    public void deserialize(ServerPlayer player, CompoundTag tag) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach((name, curio) -> curio.deserializeNBT(tag.getCompound(name))));
    }

    @Override
    public List<ItemStack> getStacks(ServerPlayer player) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (int slot = 0; slot < handler.getEquippedCurios().getSlots(); slot++) {
                stacks.add(handler.getEquippedCurios().getStackInSlot(slot));
            }
        });
        return stacks;
    }

    @Override
    public void setStack(ServerPlayer player, int slot, ItemStack stack) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getEquippedCurios().setStackInSlot(slot, stack));
    }

    @Override
    public void forEachStack(ServerPlayer player, BiConsumer<Integer, ItemStack> consumer) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (int slot = 0; slot < handler.getEquippedCurios().getSlots(); slot++) {
                consumer.accept(slot, handler.getEquippedCurios().getStackInSlot(slot));
            }
        });
    }
}
