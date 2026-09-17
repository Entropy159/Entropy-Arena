package dev.entropy159.arena.core.ui.map;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class MapListUI {
    private static final ResourceLocation ID = EntropyArena.id("maplist");

    private static ModularUI create(Player player) {
        List<ArenaMap> maps;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var mapList = ArenaData.get(server).mapList;
            maps = mapList.getAllMaps().stream().toList();
        } else {
            maps = new ArrayList<>();
        }

        return BaseUI.createDefaulted(player, root -> {
            root.addChildren(maps.stream().map(map -> new Button()
                    .setText(map.getName())
                    .setOnServerClick(e -> MapInfoUI.open(player, map))
                    .style(style -> style.color(map.isEnabled() ? 0xFF00FF00 : 0xFFFF0000))
            ).toArray(UIElement[]::new));
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> MapListUI::create);
    }
}
