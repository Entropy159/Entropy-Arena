package dev.entropy159.arena.core.ui.randomizer;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.randomizer.ItemRandomizerRegistry;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ItemRandomizerListUI {
    private static final ResourceLocation ID = EntropyArena.id("randomizer_list");

    private static ModularUI create(Player player) {
        var randomizers = ItemRandomizerRegistry.getAll();
        return BaseUI.defaultScroll(player, root -> {
            randomizers.keySet().stream().sorted((a, b) -> a.toString().compareToIgnoreCase(b.toString())).forEach(id -> {
                var randomizer = randomizers.get(id);
                root.addChild(
                        new Button()
                                .setText(randomizer.getName())
                                .setOnServerClick(e -> ItemRandomizerUI.open(player, id))
                );
            });
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> ItemRandomizerListUI::create);
    }
}
