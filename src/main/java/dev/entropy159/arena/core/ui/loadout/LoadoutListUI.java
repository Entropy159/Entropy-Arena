package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
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

public class LoadoutListUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("loadout_list");

    private final Map<String, Loadout> loadouts;

    public LoadoutListUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Loadout List"));
        loadouts = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Loadout.STREAM_CODEC).decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, root -> {
            loadouts.keySet().stream().sorted(String::compareToIgnoreCase).forEach(name -> {
                var loadout = loadouts.get(name);
                root.addChild(
                        new Button()
                                .setText(name)
                                .setOnServerClick(e -> LoadoutInfoUI.open(player, name, loadout))
                                .style(style -> style.color(loadout.isEnabled() ? 0xFF00FF00 : 0xFFFF0000))
                );
            });

            root.addChild(new Button().setText("+ New").setOnServerClick(e -> NewLoadoutUI.open(player)).style(style -> style.color(0xFF00FF00)));
        });
    }

    public static void open(Player player) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ArenaData data = ArenaData.get(server);
            PlayerUIWithData.openUI(player, ID, buf -> {
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Loadout.STREAM_CODEC).encode(buf, data.loadouts);
            });
        }
    }

    public static void register() {
        PlayerUIWithData.register(ID, LoadoutListUI::new);
    }
}
