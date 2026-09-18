package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicReference;

public class NewLoadoutUI {
    private static final ResourceLocation ID = EntropyArena.id("new_loadout");

    private static ModularUI create(Player player) {
        return BaseUI.defaultScroll(player, root -> {
            AtomicReference<String> name = new AtomicReference<>("");

            root.addChildren(
                    new Label().setText("Name"),
                    new TextField().bind(DataBindingBuilder.string(name::get, name::set).build()),
                    new Button().setText("Create").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            String newName = name.get();
                            player.closeContainer();
                            var loadouts = ArenaData.get(serverPlayer.getServer()).loadouts;
                            if (loadouts.containsKey(newName)) {
                                player.sendSystemMessage(Component.translatable("error.arena.loadout_already_exists", newName).withStyle(ChatFormatting.RED));
                            }
                            loadouts.put(newName, new Loadout(serverPlayer));
                            player.sendSystemMessage(Component.translatable("message.arena.added_loadout", newName).withStyle(ChatFormatting.GREEN));
                        }
                    }).style(style -> style.color(0xFF00FF00))
            );
        });
    }

    public static void open(Player player) {
        PlayerUIMenuType.openUI(player, ID);
    }

    public static void register() {
        PlayerUIMenuType.register(ID, player -> NewLoadoutUI::create);
    }
}
