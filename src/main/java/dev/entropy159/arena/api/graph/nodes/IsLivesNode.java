package dev.entropy159.arena.api.graph.nodes;

import com.lowdragmc.kilagraph.graph.core.AnnotatedNode;
import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.core.OutputPort;
import com.lowdragmc.kilagraph.graph.exec.EvalContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.api.gamemode.LivesGamemode;
import dev.entropy159.arena.api.graph.LoadoutTagGraph;
import dev.entropy159.arena.api.graph.MapSettingsGraph;
import net.minecraft.resources.ResourceLocation;

@NodeAttribute(name = "Is Lives", group = "Arena", graphTypes = {LoadoutTagGraph.class, MapSettingsGraph.class})
public class IsLivesNode extends AnnotatedNode {
    @InputPort
    ResourceLocation gamemode;
    @OutputPort
    boolean isLives;

    @Override
    public void evaluate(EvalContext ctx) {
        ctx.setOutput("isLives", GamemodeRegistry.getNew(ctx.getInput("gamemode", ResourceLocation.class)) instanceof LivesGamemode);
    }
}
