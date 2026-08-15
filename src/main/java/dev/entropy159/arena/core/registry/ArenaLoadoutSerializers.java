package dev.entropy159.arena.core.registry;

import dev.entropy159.arena.api.loadout.LoadoutSerializerRegistry;
import dev.entropy159.arena.core.loadout.CuriosLoadoutSerializer;
import dev.entropy159.arena.core.loadout.VanillaLoadoutSerializer;
import net.neoforged.fml.ModList;

public class ArenaLoadoutSerializers {
    public static void init() {
        LoadoutSerializerRegistry.addSerializer("vanilla", new VanillaLoadoutSerializer());

        if (ModList.get().isLoaded("curios")) {
            LoadoutSerializerRegistry.addSerializer("curios", new CuriosLoadoutSerializer());
        }
    }
}
