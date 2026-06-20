package com.entropy.arena.core.mixininterfaces;

import net.neoforged.neoforge.common.ModConfigSpec;

public interface ConfigValueAddon<T> {
    default T entropyArena$getNormal() {
        return null;
    }

    default String entropyArena$getModID() {
        return null;
    }

    default void entropyArena$setModID(String modID) {

    }

    default ModConfigSpec entropyArena$getSpec() {
        return null;
    }
}
