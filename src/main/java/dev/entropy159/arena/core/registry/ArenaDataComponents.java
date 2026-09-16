package dev.entropy159.arena.core.registry;

import com.mojang.serialization.Codec;
import dev.entropy159.arena.api.util.ArenaTeam;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ArenaDataComponents {
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, EntropyArena.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArenaTeam>> TEAM = REGISTRY.registerComponentType("team", builder -> builder.persistent(ArenaTeam.CODEC).networkSynchronized(ArenaTeam.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PEDESTAL_INDEX = REGISTRY.registerComponentType("pedestal_index", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SHOULD_DROP_ON_DEATH = REGISTRY.registerComponentType("drop_on_death", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ITEM_LIST = REGISTRY.registerComponentType("item_list", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> ITEM_RANDOMIZERS = REGISTRY.registerComponentType("item_randomizers", builder -> builder.persistent(Codec.list(ResourceLocation.CODEC)).networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
