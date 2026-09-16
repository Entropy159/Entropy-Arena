package dev.entropy159.arena.core.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Consumer;

public class BaseUI {
    public static ModularUI createDefaulted(Player player, String title, Consumer<UIElement> children) {
        return createDefaulted(player, Optional.ofNullable(title).map(Component::literal).orElse(null), children);
    }

    public static ModularUI createDefaulted(Player player, Component title, Consumer<UIElement> children) {
        if (title == null) {
            title = Component.literal("Null");
        }
        var root = new UIElement();
        root.addChild(new Label().setText(title).textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER)));
        children.accept(root);
        root.style(style -> style.background(Sprites.BORDER));
        root.layout(layout -> layout.paddingAll(7).gapAll(5));
        var ui = UI.of(root, StylesheetManager.GDP);
        return ModularUI.of(ui, player);
    }

    public static ScrollerView scrollView() {
        var scroll = new ScrollerView();
        scroll.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
        scroll.viewContainer.layout(layout -> layout.gapAll(5).paddingAll(7));
        return scroll;
    }
}
