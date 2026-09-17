package dev.entropy159.arena.core.ui;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.data.SidedDataUtils;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.itemlist.ItemListListUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutListUI;
import dev.entropy159.arena.core.ui.map.MapListUI;
import dev.entropy159.arena.core.ui.randomizer.ItemRandomizerListUI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class OverviewUI {
    private static final ResourceLocation ID = EntropyArena.id("overview");

    private static ModularUI create(Player player) {
        boolean running = SidedDataUtils.running();

        return BaseUI.createDefaulted(player, root -> {
            root.addChildren(
                    new Button().setText(running ? "Stop" : "Start").setOnServerClick(e -> {
                        var server = ServerLifecycleHooks.getCurrentServer();
                        if (server != null) {
                            if (ArenaData.get(server).running) {
                                ArenaLogic.get(server).disable();
                            } else {
                                Component error = ArenaLogic.get(server).enable();
                                if (error != null) {
                                    player.sendSystemMessage(error);
                                }
                            }
                            player.closeContainer();
                        }
                    }),
                    new Button().setText("Maps").setOnServerClick(e -> MapListUI.open(player)),
                    new Button().setText("Loadouts").setOnServerClick(e -> LoadoutListUI.open(player)),
                    new Button().setText("Item Lists").setOnServerClick(e -> ItemListListUI.open(player)),
                    new Button().setText("Item Randomizers").setOnServerClick(e -> ItemRandomizerListUI.open(player))
            );
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> OverviewUI::create);
    }
}
