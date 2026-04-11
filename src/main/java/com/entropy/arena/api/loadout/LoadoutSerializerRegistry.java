package com.entropy.arena.api.loadout;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class LoadoutSerializerRegistry {
    private static final HashMap<String, LoadoutSerializer> REGISTRY = new HashMap<>();

    public static void addSerializer(String name, LoadoutSerializer serializer) {
        if (REGISTRY.containsKey(name)) return;
        REGISTRY.put(name, serializer);
    }

    public static void forEach(BiConsumer<String, LoadoutSerializer> consumer) {
        REGISTRY.forEach(consumer);
    }

    public static CompoundTag serializeWithAll(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        forEach((name, serializer) -> tag.put(name, serializer.serialize(player)));
        return tag;
    }

    public static void deserializeWithAll(ServerPlayer player, CompoundTag tag) {
        forEach((name, serializer) -> serializer.deserialize(player, tag.getCompound(name)));
    }

    public static void forEachStack(ServerPlayer player, TriConsumer<LoadoutSerializer, Integer, ItemStack> consumer) {
        forEach((name, serializer) -> serializer.forEachStack(player, (slot, stack) -> consumer.accept(serializer, slot, stack)));
    }

    public static void clearAll(ServerPlayer player) {
        forEach((name, serializer) -> serializer.clear(player));
    }
}
