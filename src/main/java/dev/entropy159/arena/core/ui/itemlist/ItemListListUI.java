package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

public class ItemListListUI {
    private static final ResourceLocation ID = EntropyArena.id("item_list_list");

    private static ModularUI create(Player player) {
        Map<String, ItemList> itemLists;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            itemLists = ArenaData.get(server).itemLists;
        } else {
            itemLists = new HashMap<>();
        }

        return BaseUI.createDefaulted(player, root -> {
            itemLists.forEach((name, itemList) -> root.addChild(
                    new Button()
                            .setText(name)
                            .setOnServerClick(e -> ItemListInfoUI.open(player, name, itemList))
            ));

            root.addChild(new Button().setText("+ New").setOnServerClick(e -> NewItemListUI.open(player)).style(style -> style.color(0xFF00FF00)));
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> ItemListListUI::create);
    }
}
