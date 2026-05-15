package com.entropy.arena.core.registry;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ArenaStatTypes {
    public static final DeferredRegister<ResourceLocation> REGISTRY = DeferredRegister.create(Registries.CUSTOM_STAT, EntropyArena.MODID);

    public static final Supplier<ResourceLocation> MATCHES_PLAYED = register("matches_played", "Matches Played");
    public static final Supplier<ResourceLocation> MATCHES_FINISHED = register("matches_finished", "Matches Finished");
    public static final Supplier<ResourceLocation> MATCHES_WON = register("matches_won", "Matches Won");

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static Supplier<ResourceLocation> register(String name, String translation) {
        EntropyArena.REGISTRATE.addLang("stat", EntropyArena.id(name), translation);
        return REGISTRY.register(name, () -> EntropyArena.id(name));
    }
}
