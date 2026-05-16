package com.entropy.arena.api.gamemode;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GamemodeRegistry {
    public static final ResourceLocation NONE_ID = EntropyArena.id("none");
    public static final ResourceKey<Registry<Supplier<ArenaGamemode>>> REGISTRY_KEY = ResourceKey.createRegistryKey(EntropyArena.id("gamemodes"));
    public static final Registry<Supplier<ArenaGamemode>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();

    public static @Nullable ArenaGamemode getNew(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id)).map(Supplier::get).orElse(null);
    }

    public static void forEach(Consumer<ArenaGamemode> consumer) {
        REGISTRY.forEach(sup -> consumer.accept(sup.get()));
    }
}
