package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.ui.BaseUI;
import dev.entropy159.entropylib.ui.PlayerUIWithData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ItemListInfoUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("item_list_info");

    private final String name;
    private final ItemList itemList;

    public ItemListInfoUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Loadout Info"));
        name = buf.readUtf();
        itemList = ItemList.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.defaultScroll(player, root -> {
            root.layout(layout -> layout.minWidth(100));

            if (itemList.isTag()) {
                root.addChildren(new Label().setText("Tag: " + itemList.getTag().toString()));
            } else {
                root.addChild(new Button().setText("Edit ->").setOnServerClick(e -> ItemListEditorUI.open(player, itemList)).style(style -> style.color(0xFF00FF00)));
            }

            root.addChildren(
                    new Selector<ItemList.Mode>().bind(DataBindingBuilder.enumVal(ItemList.Mode.class, itemList::getMode, itemList::setMode).build()),
                    new Button().setText("Give Item").setOnClick(e -> e.currentElement.sendMessage("give")).onMessage("give", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.addItem(itemList.getItem());
                        }
                    }),
                    new Button().setText("Delete").setOnClick(e -> e.currentElement.sendMessage("delete")).style(style -> style.color(0xFFFF0000)).onMessage("delete", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            ArenaData.get(serverPlayer.getServer()).itemLists.remove(name);
                        }
                    })
            );
        });
    }

    public static void open(Player player, String name, ItemList list) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            buf.writeUtf(name);
            ItemList.STREAM_CODEC.encode(buf, list);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, ItemListInfoUI::new);
    }
}
