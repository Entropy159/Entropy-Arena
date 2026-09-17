package dev.entropy159.arena.core.ui.itemlist;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.ItemList;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NewItemListUI {
    private static final ResourceLocation ID = EntropyArena.id("new_item_list");

    private static ModularUI create(Player player) {
        return BaseUI.createDefaulted(player, root -> {
            root.layout(layout -> layout.minWidth(100));

            AtomicReference<String> name = new AtomicReference<>("");
            AtomicReference<ItemList.Mode> mode = new AtomicReference<>(ItemList.Mode.BOTH);
            AtomicBoolean isTag = new AtomicBoolean();
            AtomicReference<String> tagKey = new AtomicReference<>("");

            List<String> possibleTags = new ArrayList<>(player.level().registryAccess().registry(Registries.ITEM).orElseThrow().getTagNames().map(tag -> tag.location().toString()).toList());
            tagKey.set(possibleTags.getFirst());

            root.addChildren(
                    new Label().setText("Name"),
                    new TextField().bind(DataBindingBuilder.string(name::get, name::set).build()),
                    new Toggle().setText("Is Tag").bind(DataBindingBuilder.bool(isTag::get, isTag::set).build()),
                    new Label().setText("Tag"),
                    new Selector<String>().setCandidates(possibleTags).bind(DataBindingBuilder.string(tagKey::get, tagKey::set).build()),
                    new Button().setText("Create").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            String newName = name.get();
                            String tagVal = tagKey.get();
                            ItemList.Mode newMode = mode.get();

                            player.closeContainer();

                            var itemLists = ArenaData.get(serverPlayer.getServer()).itemLists;
                            if (itemLists.containsKey(newName)) {
                                player.sendSystemMessage(Component.translatable("error.arena.item_list_already_exists", newName).withStyle(ChatFormatting.RED));
                            }
                            AtomicReference<TagKey<Item>> tag = new AtomicReference<>();
                            if (isTag.get()) {
                                Optional.ofNullable(ResourceLocation.tryParse(tagVal)).ifPresent(t -> tag.set(TagKey.create(Registries.ITEM, t)));
                            }
                            itemLists.put(newName, new ItemList(newName, newMode, tag.get()));
                            player.sendSystemMessage(Component.translatable("message.arena.added_item_list", newName).withStyle(ChatFormatting.GREEN));
                        }
                    }).style(style -> style.color(0xFF00FF00))
            );
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> NewItemListUI::create);
    }
}
