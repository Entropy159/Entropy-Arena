package com.entropy.arena.core.registry;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArenaSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, EntropyArena.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ARENA_SOUND = REGISTRY.register("music.arena", () -> SoundEvent.createVariableRangeEvent(EntropyArena.id("music.arena")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LOBBY_SOUND = REGISTRY.register("music.lobby", () -> SoundEvent.createVariableRangeEvent(EntropyArena.id("music.lobby")));
    public static final Music ARENA_MUSIC = new Music(ARENA_SOUND, 2, 5, true);
    public static final Music LOBBY_MUSIC = new Music(LOBBY_SOUND, 2, 5, true);

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
