package dev.entropy159.arena.core.ui.map;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.nodegraphtookit.gui.GraphView;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.ui.BaseUI;
import dev.entropy159.entropylib.ui.PlayerUIWithData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class MapLoadoutGraphUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("map_loadout_graph");

    private ArenaMap map;

    public MapLoadoutGraphUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Map Loadout Graph"));
        map = ArenaMap.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            map = ArenaData.get(serverPlayer.getServer()).mapList.getMap(map.getName());
        }

        var editorView = new GraphView();
        editorView.layout(layout -> {
            layout.widthPercent(95);
            layout.heightPercent(95);
        });
        editorView.loadGraph(map.getLoadoutTagGraph());
        editorView.onMessage("save", tag -> {
            map.updateLoadoutGraph(tag, player.level().registryAccess());
        });
        editorView.addCommandListener((command, view, model) -> {
            editorView.sendMessage("save", model.serializeNBT(player.level().registryAccess()));
        });
        return BaseUI.createBase(player, editorView);
    }

    public static void open(ServerPlayer player, ArenaMap map) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            ArenaMap.STREAM_CODEC.encode(buf, map);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, MapLoadoutGraphUI::new);
    }
}
