package com.entropy.arena.api.gamemode;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GamemodeRegistry {
    public static final ResourceLocation NONE_ID = EntropyArena.id("none");
    private static final HashMap<ResourceLocation, Supplier<ArenaGamemode>> GAMEMODE_REGISTRY = new HashMap<>();

    public static void registerGamemode(Supplier<ArenaGamemode> gamemode) {
        GAMEMODE_REGISTRY.put(gamemode.get().getRegistryID(), gamemode);
    }

    public static @Nullable ArenaGamemode getGamemode(ResourceLocation id) {
        Supplier<ArenaGamemode> supplier = GAMEMODE_REGISTRY.get(id);
        return supplier == null ? null : supplier.get();
    }

    public static void forEach(Consumer<ArenaGamemode> consumer) {
        GAMEMODE_REGISTRY.values().forEach(supplier -> consumer.accept(supplier.get()));
    }
}
