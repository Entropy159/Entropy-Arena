package dev.entropy159.arena.api.loadout.graph.nodes;

import com.lowdragmc.kilagraph.graph.core.AnnotatedNode;
import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.core.OutputPort;
import com.lowdragmc.kilagraph.graph.exec.EvalContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import dev.entropy159.arena.api.gamemode.CoOpGamemode;
import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.api.loadout.graph.LoadoutTagGraph;
import net.minecraft.resources.ResourceLocation;

@NodeAttribute(name = "Is Co-Op", group = "Arena", graphTypes = {LoadoutTagGraph.class})
public class IsCoOpNode extends AnnotatedNode {
    @InputPort
    ResourceLocation gamemode;
    @OutputPort
    boolean isCoOp;

    @Override
    public void evaluate(EvalContext ctx) {
        ctx.setOutput("isCoOp", GamemodeRegistry.getNew(ctx.getInput("gamemode", ResourceLocation.class)) instanceof CoOpGamemode);
    }
}
