package dev.entropy159.arena.core.ui.loadout;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Inspector;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.network.toServer.LoadoutEditPacket;
import dev.entropy159.arena.core.network.toServer.UpdateLoadoutPacket;
import dev.entropy159.arena.core.ui.BaseUI;
import dev.entropy159.entropylib.ui.CustomPlayerUIMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class LoadoutInfoUI extends CustomPlayerUIMenuType.CustomPlayerUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("loadout_info");

    private String name;
    private Loadout loadout;

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        return BaseUI.createDefaulted(player, name, root -> {
            var inspector = new Inspector();
            if (loadout != null) inspector.inspect(loadout, configurator -> {
                if (FMLEnvironment.dist.isClient()) {
                    PacketDistributor.sendToServer(new UpdateLoadoutPacket(name, loadout));
                } else {
                    assert ServerLifecycleHooks.getCurrentServer() != null;
                    ArenaData.get(ServerLifecycleHooks.getCurrentServer()).loadouts.get(name).updateFrom(loadout);
                }
            });
            root.addChild(inspector);
            root.addChildren(
                    new Button().setText("Give").setOnClick(e -> {
                        PacketDistributor.sendToServer(new LoadoutEditPacket(name, false));
                    }),
                    new Button().setText("Save").setOnClick(e -> {
                        PacketDistributor.sendToServer(new LoadoutEditPacket(name, true));
                    }),
                    new Button().setText("Delete").setOnClick(e -> e.currentElement.sendMessage("delete", TagBuilder.compound().add("name", name).build())).style(style -> style.color(0xFFFF0000)).onMessage("delete", tag -> {
                        if (player instanceof ServerPlayer serverPlayer) {
                            player.closeContainer();
                            ArenaData.get(serverPlayer.getServer()).loadouts.remove(tag.getString("name"));
                        }
                    })
            );
        });
    }

    @Override
    public void loadData(@NotNull RegistryFriendlyByteBuf buf) {
        name = buf.readUtf();
        loadout = Loadout.STREAM_CODEC.decode(buf);
    }

    public static void write(String name, Loadout loadout, RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name);
        Loadout.STREAM_CODEC.encode(buf, loadout);
    }

    public static void open(Player player, String name, Loadout loadout) {
        CustomPlayerUIMenuType.openUI(player, ID, buf -> write(name, loadout, buf));
    }

    public static void register() {
        CustomPlayerUIMenuType.register(ID, player -> new LoadoutInfoUI());
    }
}
