package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemListListUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("item_list_list");

    private final Map<String, ItemList> itemLists;

    public ItemListListUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Item Lists"));
        itemLists = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ItemList.STREAM_CODEC).decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, root -> {
            itemLists.keySet().stream().sorted(String::compareToIgnoreCase).forEach(name -> {
                var itemList = itemLists.get(name);
                root.addChild(
                        new Button()
                                .setText(name)
                                .setOnServerClick(e -> ItemListInfoUI.open(player, name, itemList))
                );
            });

            root.addChild(new Button().setText("+ New").setOnServerClick(e -> NewItemListUI.open(player)).style(style -> style.color(0xFF00FF00)));
        });
    }

    public static void open(Player player) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ArenaData data = ArenaData.get(server);
            PlayerUIWithData.openUI(player, ID, buf -> {
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ItemList.STREAM_CODEC).encode(buf, data.itemLists);
            });
        }
    }

    public static void register() {
        PlayerUIWithData.register(ID, ItemListListUI::new);
    }
}
