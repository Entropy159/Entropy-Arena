package dev.entropy159.arena.core.registry;

import dev.entropy159.arena.core.ui.OverviewUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutInfoUI;
import dev.entropy159.arena.core.ui.loadout.LoadoutListUI;
import dev.entropy159.arena.core.ui.loadout.NewLoadoutUI;
import dev.entropy159.arena.core.ui.map.MapInfoUI;
import dev.entropy159.arena.core.ui.map.MapListUI;
import dev.entropy159.arena.core.ui.randomizer.ItemRandomizerListUI;
import dev.entropy159.arena.core.ui.randomizer.ItemRandomizerUI;

public class ArenaMenuRegistry {
    public static void init() {
        OverviewUI.register();
        MapListUI.register();
        MapInfoUI.register();
        LoadoutListUI.register();
        LoadoutInfoUI.register();
        NewLoadoutUI.register();
        ItemRandomizerListUI.register();
        ItemRandomizerUI.register();
    }
}
