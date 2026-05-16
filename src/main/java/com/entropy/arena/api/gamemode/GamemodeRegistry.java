package com.entropy.arena.api.gamemode;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Consumer;

public class GamemodeRegistry {
    public static final ResourceLocation NONE_ID = EntropyArena.id("none");
    public static final ResourceKey<Registry<ArenaGamemode>> REGISTRY_KEY = ResourceKey.createRegistryKey(EntropyArena.id("gamemodes"));
    public static final Registry<ArenaGamemode> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    public static ArenaGamemode get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static void forEach(Consumer<ArenaGamemode> consumer) {
        REGISTRY.forEach(consumer);
    }
}
