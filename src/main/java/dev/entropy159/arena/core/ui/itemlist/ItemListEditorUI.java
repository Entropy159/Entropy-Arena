package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ItemListEditorUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("item_list_editor");

    private final ItemList itemList;

    public ItemListEditorUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Item List Editor"));
        itemList = ItemList.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, root -> {
            //TODO: slots
            root.addChild(new InventorySlots());
        });
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
