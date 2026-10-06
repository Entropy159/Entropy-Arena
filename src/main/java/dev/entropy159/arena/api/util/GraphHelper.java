package dev.entropy159.arena.api.util;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.Graph;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.variable.VariableKind;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.SpawnFlags;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.VariableNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.VariableDeclarationModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.VariableDeclarationModelBase;
import org.joml.Vector2f;

import java.lang.reflect.Type;

public class GraphHelper {
    public static VariableDeclarationModel createVariable(Graph graph, String name, Type type, Object defaultValue, VariableKind kind) {
        return (VariableDeclarationModel) graph.graphModel.createVariable(name, type, defaultValue, kind);
    }

    public static VariableNodeModel createVariableNode(Graph graph, VariableDeclarationModelBase variable, Vector2f pos) {
        return graph.graphModel.createVariableNode(variable, pos, null, SpawnFlags.DEFAULT);
    }
}
