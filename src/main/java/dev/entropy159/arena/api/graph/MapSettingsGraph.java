package dev.entropy159.arena.api.graph;

import com.lowdragmc.kilagraph.blueprint.BlueprintGraph;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.GraphNodeRegistry;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class MapSettingsGraph extends BlueprintGraph {
    public static final GraphNodeRegistry NODE_REGISTRY = GraphNodeRegistry.create(EntropyArena.id("map_settings"), MapSettingsGraph.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, MapSettingsGraph> STREAM_CODEC = StreamCodec.of((buf, val) -> {
        ByteBufCodecs.COMPOUND_TAG.encode(buf, val.graphModel.serializeNBT(buf.registryAccess()));
    }, buf -> {
        var graph = new MapSettingsGraph();
        graph.graphModel.deserializeNBT(buf.registryAccess(), ByteBufCodecs.COMPOUND_TAG.decode(buf));
        return graph;
    });

    @Override
    public List<Class<? extends Node>> getSupportNodes() {
        var list = new ArrayList<>(NODE_REGISTRY.getNodeClasses());
        list.addAll(super.getSupportNodes());
        return list;
    }
}
