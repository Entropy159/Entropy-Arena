package dev.entropy159.arena.api.loadout.graph;

import com.lowdragmc.kilagraph.blueprint.BlueprintGraph;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.GraphNodeRegistry;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import dev.entropy159.arena.core.EntropyArena;

import java.util.ArrayList;
import java.util.List;

public class LoadoutTagGraph extends BlueprintGraph {
    public static final GraphNodeRegistry NODE_REGISTRY = GraphNodeRegistry.create(EntropyArena.id("loadout_tag"), LoadoutTagGraph.class);

    @Override
    public List<Class<? extends Node>> getSupportNodes() {
        var list = new ArrayList<>(NODE_REGISTRY.getNodeClasses());
        list.addAll(super.getSupportNodes());
        return list;
    }
}
