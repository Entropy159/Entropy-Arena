package com.entropy.arena.core.registry;

import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.core.loadout.VanillaLoadoutSerializer;

public class ArenaLoadoutSerializers {
    public static void init() {
        LoadoutSerializerRegistry.addSerializer("vanilla", new VanillaLoadoutSerializer());
    }
}
