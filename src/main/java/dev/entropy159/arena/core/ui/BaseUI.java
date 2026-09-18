package dev.entropy159.arena.core.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.entropy159.arena.core.config.ClientConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Consumer;

public class BaseUI {
    public static ModularUI defaultScroll(Player player, Consumer<UIElement> children) {
        var root = createBaseScroll();
        root.addClass("panel_bg");
        children.accept(root.viewContainer);
        return createBase(player, root);
    }

    public static ModularUI createBase(Player player, UIElement root) {
        ResourceLocation theme = StylesheetManager.GDP;
        if (FMLEnvironment.dist.isClient()) {
            theme = ResourceLocation.parse(ClientConfig.UI_THEME.get());
        }
        var ui = UI.of(root, theme);
        return ModularUI.of(ui, player);
    }

    public static UIElement createBasePanel() {
        var element = new UIElement();
        element.layout(layout -> layout.gapAll(5).paddingAll(7).maxHeight(200).maxWidth(600));
        return element;
    }

    public static ScrollerView createBaseScroll() {
        var element = new ScrollerView();
        element.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
        element.viewContainer.layout(layout -> layout.gapAll(5).paddingAll(7).maxHeight(200).maxWidth(600));
        return element;
    }
}
