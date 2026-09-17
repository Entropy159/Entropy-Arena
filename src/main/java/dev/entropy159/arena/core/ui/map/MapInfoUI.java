package dev.entropy159.arena.core.ui.map;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Inspector;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class MapInfoUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("mapinfo");

    private final ArenaMap map;

    public MapInfoUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Map Info"));
        map = ArenaMap.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        if (map == null) {
            return BaseUI.createDefaulted(player, root -> {
            });
        }
        return BaseUI.createDefaulted(player, root -> {
            var inspector = new Inspector();
            root.addChild(inspector);
            inspector.inspect(map, configurator -> inspector.sendMessage("update", TagBuilder.compound().add("map", map.toTag()).build()));
            inspector.onMessage("update", tag -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    ArenaLogic.get(serverPlayer.getServer()).updateMap(ArenaMap.fromTag(tag.getCompound("map")));
                }
            });

            root.addChildren(
                    new Button().setText("Update").setOnClick(e -> e.currentElement.sendMessage("update", TagBuilder.compound().add("name", map.getName()).build())).style(style -> style.color(0xFF00FF00)).onMessage("update", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            var map = ArenaData.get(serverPlayer.server).mapList.getMap(tag.getString("name"));
                            if (map != null) {
                                player.closeContainer();
                                map.update(serverPlayer.serverLevel(), serverPlayer);
                            }
                        }
                    }),
                    new Button().setText("Load").setOnClick(e -> e.currentElement.sendMessage("load", TagBuilder.compound().add("name", map.getName()).build())).onMessage("update", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            var map = ArenaData.get(serverPlayer.server).mapList.getMap(tag.getString("name"));
                            if (map != null) {
                                player.closeContainer();
                                map.load(serverPlayer.serverLevel());
                                var pos = map.getCenter();
                                serverPlayer.teleportTo(pos.x, pos.y, pos.z);
                            }
                        }
                    }),
                    new Button().setText("Remove").setOnClick(e -> e.currentElement.sendMessage("remove", TagBuilder.compound().add("name", map.getName()).build())).style(style -> style.color(0xFFFF0000)).onMessage("remove", tag -> {
                        ArenaData.get(ServerLifecycleHooks.getCurrentServer()).mapList.removeMap(tag.getString("name"));
                        player.closeContainer();
                    })
            );
        });
    }

    public static void open(Player player, ArenaMap map) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            ArenaMap.STREAM_CODEC.encode(buf, map);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, MapInfoUI::new);
    }
}
