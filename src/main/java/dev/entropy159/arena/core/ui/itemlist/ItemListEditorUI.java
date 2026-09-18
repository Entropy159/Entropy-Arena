package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.slot.ItemHandlerSlot;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ItemListEditorUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("item_list_editor");

    private ItemList itemList;

    public ItemListEditorUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Item List Editor"));
        itemList = ItemList.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            itemList = ArenaData.get(serverPlayer.getServer()).itemLists.get(itemList.getName());
        }
        return BaseUI.createDefaulted(player, root -> {
            var panel = new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW).flexWrap(FlexWrap.WRAP));
            for (int index = 0; index <= itemList.size(); index++) {
                addSlot(panel, index);
            }
            root.addChildren(panel, new InventorySlots());
        });
    }

    private void addSlot(UIElement panel, int index) {
        var slot = new ItemSlot(new ItemHandlerSlot(itemList.getHandler(), index).addChangeListener(() -> {
            while (panel.getSafeChildren().size() <= itemList.size()) {
                addSlot(panel, panel.getSafeChildren().size());
            }
        }));
        panel.addChild(slot);
    }

    public static void open(Player player, ItemList list) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            ItemList.STREAM_CODEC.encode(buf, list);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, ItemListEditorUI::new);
    }
}
