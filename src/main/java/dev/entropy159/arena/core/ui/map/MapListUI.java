package dev.entropy159.arena.core.ui.map;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
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

import java.util.List;

public class MapListUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("maplist");

    private final List<ArenaMap> maps;

    public MapListUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Map List"));
        maps = ArenaMap.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, root -> {
            root.addChildren(maps.stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).map(map -> new Button()
                    .setText(map.getName())
                    .setOnServerClick(e -> MapInfoUI.open(player, map))
                    .style(style -> style.color(map.isEnabled() ? 0xFF00FF00 : 0xFFFF0000))
            ).toArray(UIElement[]::new));
        });
    }

    public static void open(Player player) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var data = ArenaData.get(server);
            PlayerUIWithData.openUI(player, ID, buf -> {
                ArenaMap.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.mapList.getAllMaps());
            });
        }
    }

    public static void register() {
        PlayerUIWithData.register(ID, MapListUI::new);
    }
}
