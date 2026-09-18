package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoadoutSelectionUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("loadout_selection");

    private final List<String> loadouts;

    public LoadoutSelectionUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Loadout Selection"));
        loadouts = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, root -> {
            loadouts.stream().sorted(String::compareToIgnoreCase).forEach(loadout -> {
                root.addChild(new Button().setText(loadout).setOnServerClick(e -> {
                    player.closeContainer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        ArenaLogic.get(serverPlayer.getServer()).selectLoadout(serverPlayer, loadout);
                    }
                }));
            });
        });
    }

    public static void open(ServerPlayer player) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, ArenaLogic.get(player.getServer()).getValidLoadouts(player).keySet().stream().toList());
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, LoadoutSelectionUI::new);
    }
}
