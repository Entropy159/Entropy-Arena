package dev.entropy159.arena.core.registry;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import dev.entropy159.arena.api.ui.PlayerUIWithData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.OverviewUI;
import dev.entropy159.arena.core.ui.itemlist.ItemListEditorUI;
import dev.entropy159.arena.core.ui.itemlist.ItemListInfoUI;
import dev.entropy159.arena.core.ui.itemlist.ItemListListUI;
import dev.entropy159.arena.core.ui.itemlist.NewItemListUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutInfoUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutListUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutSelectionUI;
import dev.entropy159.arena.core.ui.loadout.NewLoadoutUI;
import dev.entropy159.arena.core.ui.map.MapInfoUI;
import dev.entropy159.arena.core.ui.map.MapListUI;
import dev.entropy159.arena.core.ui.randomizer.ItemRandomizerListUI;
import dev.entropy159.arena.core.ui.randomizer.ItemRandomizerUI;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ArenaMenuRegistry {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, EntropyArena.MODID);
    public static final Supplier<MenuType<ModularUIContainerMenu>> PLAYER_UI_DATA = REGISTRY.register("player_ui_data",
            () -> IMenuTypeExtension.create(PlayerUIWithData::create));

    public static void init(IEventBus bus) {
        REGISTRY.register(bus);

        OverviewUI.register();
        MapListUI.register();
        MapInfoUI.register();
        LoadoutListUI.register();
        LoadoutInfoUI.register();
        NewLoadoutUI.register();
        ItemRandomizerListUI.register();
        ItemRandomizerUI.register();
        ItemListListUI.register();
        ItemListInfoUI.register();
        NewItemListUI.register();
        ItemListEditorUI.register();
        LoadoutSelectionUI.register();
    }
}
