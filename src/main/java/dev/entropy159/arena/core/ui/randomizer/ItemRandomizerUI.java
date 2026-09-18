package dev.entropy159.arena.core.ui.randomizer;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import dev.entropy159.arena.api.randomizer.ItemRandomizerRegistry;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.ui.BaseUI;
import dev.entropy159.entropylib.ui.PlayerUIWithData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemRandomizerUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("randomizer_info");

    private final ResourceLocation id;

    public ItemRandomizerUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Item Randomizer"));
        id = buf.readResourceLocation();
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.defaultScroll(player, root -> {
            root.addChildren(
                    new Button().setText("Apply").setOnClick(e -> e.currentElement.sendMessage("apply")).onMessage("apply", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            Optional.ofNullable(ItemRandomizerRegistry.get(id)).ifPresent(r -> {
                                r.apply(serverPlayer.getMainHandItem());
                                serverPlayer.closeContainer();
                            });
                        }
                    }).style(style -> style.color(0xFF00FF00)),
                    new Button().setText("Remove").setOnClick(e -> e.currentElement.sendMessage("remove")).onMessage("remove", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            Optional.ofNullable(ItemRandomizerRegistry.get(id)).ifPresent(r -> {
                                r.remove(serverPlayer.getMainHandItem());
                                serverPlayer.closeContainer();
                            });
                        }
                    }).style(style -> style.color(0xFFFF0000))
            );
        });
    }

    public static void open(Player player, ResourceLocation id) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            buf.writeResourceLocation(id);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, ItemRandomizerUI::new);
    }
}
