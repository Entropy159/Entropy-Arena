package dev.entropy159.arena.api.ui;

import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import dev.entropy159.arena.core.registry.ArenaMenuRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerUIWithData {
    private final static Map<ResourceLocation, BiFunction<Player, RegistryFriendlyByteBuf, DataUIHolder>> UI_HOLDERS = new ConcurrentHashMap<>();

    public static void register(ResourceLocation id, BiFunction<Player, RegistryFriendlyByteBuf, DataUIHolder> holder) {
        UI_HOLDERS.put(id, holder);
    }

    public static void unregister(ResourceLocation id) {
        UI_HOLDERS.remove(id);
    }

    /**
     * Opens a UI for the specified player if the given identifier is registered.
     * This method checks if a corresponding UI holder exists for the provided id,
     * creates the holder instance using the associated provider, and opens the menu for the player.
     *
     * @param player the {@link Player} for whom the UI should be opened
     * @param id     the {@link ResourceLocation} identifier of the UI to be opened
     * @return {@code true} if the UI was successfully opened, {@code false} if the id is not registered
     * or the holder instance could not be created
     */
    public static boolean openUI(Player player, ResourceLocation id, Consumer<RegistryFriendlyByteBuf> customData) {
        if (!UI_HOLDERS.containsKey(id)) return false;
        var tempBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.level().registryAccess(), ConnectionType.NEOFORGE);
        customData.accept(tempBuf);
        DataUIHolder holder = UI_HOLDERS.get(id).apply(player, tempBuf);
        tempBuf.release();
        if (holder == null) return false;
        player.openMenu(holder, buf -> {
            buf.writeResourceLocation(id);
            customData.accept(buf);
        });
        return true;
    }

    public static ModularUIContainerMenu create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
        var id = data.readResourceLocation();
        var holder = UI_HOLDERS.get(id).apply(inv.player, data);
        if (holder == null) throw new IllegalArgumentException("No player ui holder found for id " + id);
        return new ModularUIContainerMenu(LDMenuTypes.PLAYER_UI.get(), windowId, inv, holder);
    }

    public static abstract class DataUIHolder implements MenuProvider, IContainerUIHolder {
        private final Component displayName;

        public DataUIHolder(Component name) {
            displayName = name;
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }

        @Override
        public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new ModularUIContainerMenu(ArenaMenuRegistry.PLAYER_UI_DATA.get(), containerId, playerInventory, this);
        }

        @Override
        public boolean isStillValid(Player player) {
            return player.isAlive();
        }
    }
}
