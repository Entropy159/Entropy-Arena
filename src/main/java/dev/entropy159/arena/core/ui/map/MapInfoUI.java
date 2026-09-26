package dev.entropy159.arena.core.ui.map;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Inspector;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.ui.BaseUI;
import dev.entropy159.entropylib.ui.PlayerUIWithData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
            return BaseUI.defaultScroll(player, root -> {
            });
        }
        return BaseUI.defaultScroll(player, root -> {
            var inspector = new Inspector();
            root.addChild(inspector);
            inspector.inspect(map, configurator -> inspector.sendMessage("update", TagBuilder.compound().add("map", map.toTag()).build()));
            inspector.onMessage("update", tag -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    ArenaLogic.get(serverPlayer.getServer()).updateMap(ArenaMap.fromTag(tag.getCompound("map")));
                }
            });

            var server = ServerLifecycleHooks.getCurrentServer();
            var map = server == null ? this.map : ArenaData.get(server).mapList.getMap(this.map.getName());
            assert map != null;

            root.addChildren(
                    new Button().setText("Update").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            map.update(serverPlayer.serverLevel(), serverPlayer);
                        }
                    }).style(style -> style.color(0xFF00FF00)),
                    new Button().setText("Validate").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            if (serverPlayer.serverLevel().dimension() == map.getDimension()) {
                                var result = map.validate(serverPlayer.serverLevel());
                                serverPlayer.sendSystemMessage(Objects.requireNonNullElseGet(result, () -> Component.translatable("message.arena.map_validated")));
                            } else {
                                serverPlayer.sendSystemMessage(Component.translatable("error.arena.wrong_dimension", map.getDimension().location()));
                            }
                        }
                    }).style(style -> style.color(0xFF00FF00)),
                    new Button().setText("Teleport").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            var pos = map.getCenter();
                            serverPlayer.teleportTo(pos.x, pos.y, pos.z);
                        }
                    }),
                    new Button().setText("Load").setOnServerClick(e -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            map.load(serverPlayer.serverLevel());
                        }
                    }),
                    new Button().setText("Remove").setOnServerClick(e -> {
                        ArenaData.get(ServerLifecycleHooks.getCurrentServer()).mapList.removeMap(map.getName());
                        player.closeContainer();
                    }).style(style -> style.color(0xFFFF0000))
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
