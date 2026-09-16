package dev.entropy159.arena.core.ui.randomizer;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.entropy159.arena.api.randomizer.ItemRandomizerRegistry;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import dev.entropy159.entropylib.ui.CustomPlayerUIMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemRandomizerUI extends CustomPlayerUIMenuType.CustomPlayerUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("randomizer_info");

    private ResourceLocation id = ResourceLocation.withDefaultNamespace("none");

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        var randomizer = ItemRandomizerRegistry.get(id);
        if (randomizer == null) {
            return BaseUI.createDefaulted(player, "", root -> {});
        }
        return BaseUI.createDefaulted(player, randomizer.getName(), root -> {
            root.addChildren(
                    new Button().setText("Apply").setOnClick(e -> e.currentElement.sendMessage("apply", TagBuilder.compound().add("id", id.toString()).build())).onMessage("apply", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            Optional.ofNullable(ItemRandomizerRegistry.get(ResourceLocation.parse(tag.getString("id")))).ifPresent(r -> {
                                r.apply(serverPlayer.getMainHandItem());
                                serverPlayer.closeContainer();
                            });
                        }
                    }).style(style -> style.color(0xFF00FF00)),
                    new Button().setText("Remove").setOnClick(e -> e.currentElement.sendMessage("remove", TagBuilder.compound().add("id", id.toString()).build())).onMessage("remove", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            Optional.ofNullable(ItemRandomizerRegistry.get(ResourceLocation.parse(tag.getString("id")))).ifPresent(r -> {
                                r.remove(serverPlayer.getMainHandItem());
                                serverPlayer.closeContainer();
                            });
                        }
                    }).style(style -> style.color(0xFFFF0000))
            );
        });
    }

    @Override
    public void loadData(@NotNull RegistryFriendlyByteBuf buf) {
        id = buf.readResourceLocation();
    }

    public static void write(ResourceLocation id, RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
    }

    public static void open(Player player, ResourceLocation id) {
        CustomPlayerUIMenuType.openUI(player, ID, buf -> write(id, buf));
    }

    public static void register() {
        CustomPlayerUIMenuType.register(ID, player -> new ItemRandomizerUI());
    }
}
