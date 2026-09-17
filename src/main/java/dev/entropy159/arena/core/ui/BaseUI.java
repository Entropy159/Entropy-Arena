package dev.entropy159.arena.core.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class BaseUI {
    public static ModularUI createDefaulted(Player player, Consumer<UIElement> children) {
        var root = new ScrollerView();
        root.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
        root.viewContainer.layout(layout -> layout.gapAll(5).paddingAll(7));
        root.addClass("panel_bg");
        root.viewContainer.layout(layout -> layout.maxHeight(200));
        children.accept(root.viewContainer);
        var ui = UI.of(root, StylesheetManager.MODERN);
        return ModularUI.of(ui, player);
    }
}
