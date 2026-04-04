package com.entropy.arena.core.registry;

import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.core.loadout.CuriosLoadoutSerializer;
import com.entropy.arena.core.loadout.VanillaLoadoutSerializer;
import net.neoforged.fml.ModList;

public class ArenaLoadoutSerializers {
    public static void init() {
        LoadoutSerializerRegistry.addSerializer("vanilla", new VanillaLoadoutSerializer());

        if (ModList.get().isLoaded("curios")) {
            LoadoutSerializerRegistry.addSerializer("curios", new CuriosLoadoutSerializer());
        }
    }
}
