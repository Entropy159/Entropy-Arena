package dev.entropy159.arena.api.loadout.graph.nodes;

import com.lowdragmc.kilagraph.graph.core.AnnotatedNode;
import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.core.OutputPort;
import com.lowdragmc.kilagraph.graph.exec.EvalContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.api.loadout.graph.LoadoutTagGraph;

import java.util.List;

@NodeAttribute(name = "Get Tags", group = "Arena", graphTypes = {LoadoutTagGraph.class})
public class GetTagsNode extends AnnotatedNode {
    @InputPort
    Loadout loadout;
    @OutputPort
    List<String> tags;

    @Override
    public void evaluate(EvalContext ctx) {
        ctx.setOutput("tags", ctx.getInput("tags", Loadout.class).getTags());
    }
}
