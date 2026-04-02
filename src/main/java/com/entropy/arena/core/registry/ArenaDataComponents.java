package com.entropy.arena.core.registry;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.EntropyArena;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ArenaDataComponents {
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, EntropyArena.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArenaTeam>> TEAM = REGISTRY.registerComponentType("team", builder -> builder.persistent(ArenaTeam.CODEC).networkSynchronized(ArenaTeam.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PEDESTAL_INDEX = REGISTRY.registerComponentType("pedestal_index", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SHOULD_DROP_ON_DEATH = REGISTRY.registerComponentType("drop_on_death", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
