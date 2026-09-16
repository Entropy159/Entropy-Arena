package dev.entropy159.arena.api.randomizer;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class ItemRandomizerRegistry {
    private static final HashMap<ResourceLocation, ItemRandomizer> REGISTRY = new HashMap<>();

    public static boolean addRandomizer(ResourceLocation id, ItemRandomizer randomizer) {
        if (REGISTRY.containsKey(id)) return false;
        randomizer.setID(id);
        REGISTRY.put(id, randomizer);
        return true;
    }

    public static void forEach(BiConsumer<ResourceLocation, ItemRandomizer> consumer) {
        REGISTRY.forEach(consumer);
    }

    public static @Nullable ItemRandomizer get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static HashMap<ResourceLocation, ItemRandomizer> getAll() {
        return new HashMap<>(REGISTRY);
    }
}
