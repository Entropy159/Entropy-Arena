package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Inspector;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.api.loadout.LoadoutSerializerRegistry;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.BaseUI;
import dev.entropy159.entropylib.util.Utils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class LoadoutInfoUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("loadout_info");

    private final String name;
    private final Loadout loadout;

    public LoadoutInfoUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Loadout Info"));
        name = buf.readUtf();
        loadout = Loadout.STREAM_CODEC.decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.defaultScroll(player, root -> {
            var inspector = new Inspector();
            inspector.inspect(loadout, configurator -> inspector.sendMessage("update", TagBuilder.compound().add("enabled", loadout.isEnabled()).add("tags", Utils.listToTag(loadout.getTags(), StringTag::valueOf)).build()));
            inspector.onMessage("update", tag -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    var loadout = ArenaData.get(serverPlayer.getServer()).loadouts.get(name);
                    loadout.setEnabled(tag.getBoolean("enabled"));
                    loadout.setTags(Utils.tagToArrayList(tag.getList("tags", Tag.TAG_STRING), Tag::getAsString));
                }
            });
            root.addChild(inspector);
            root.addChildren(
                    new Button().setText("Give").setOnClick(e -> e.currentElement.sendMessage("give")).onMessage("give", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            ArenaData.get(serverPlayer.getServer()).loadouts.get(name).giveToPlayer(serverPlayer);
                        }
                    }),
                    new Button().setText("Save").setOnClick(e -> e.currentElement.sendMessage("save")).onMessage("save", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            ArenaData.get(serverPlayer.getServer()).loadouts.put(name, new Loadout(serverPlayer));
                            LoadoutSerializerRegistry.clearAll(serverPlayer);
                        }
                    }),
                    new Button().setText("Delete").setOnClick(e -> e.currentElement.sendMessage("delete")).style(style -> style.color(0xFFFF0000)).onMessage("delete", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            ArenaData.get(serverPlayer.getServer()).loadouts.remove(name);
                        }
                    })
            );
        });
    }

    public static void open(Player player, String name, Loadout loadout) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            buf.writeUtf(name);
            Loadout.STREAM_CODEC.encode(buf, loadout);
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, LoadoutInfoUI::new);
    }
}
