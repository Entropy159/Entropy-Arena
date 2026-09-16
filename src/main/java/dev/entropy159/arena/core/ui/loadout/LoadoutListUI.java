package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

public class LoadoutListUI {
    private static final ResourceLocation ID = EntropyArena.id("loadout_list");

    private static ModularUI create(Player player) {
        Map<String, Loadout> loadouts;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            loadouts = ArenaData.get(server).loadouts;
        } else {
            loadouts = new HashMap<>();
        }

        return BaseUI.createDefaulted(player, "Loadouts", root -> {
            var scroll = BaseUI.scrollView();
            loadouts.forEach((name, loadout) -> scroll.addScrollViewChild(
                    new Button()
                            .setText(name)
                            .setOnServerClick(e -> LoadoutInfoUI.open(player, name, loadout))
                            .style(style -> style.color(loadout.isEnabled() ? 0xFF00FF00 : 0xFFFF0000))
            ));
            root.addChild(scroll);

            root.addChild(new Button().setText("+ New").setOnServerClick(e -> NewLoadoutUI.open(player)).style(style -> style.color(0xFF00FF00)));
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> LoadoutListUI::create);
    }
}
