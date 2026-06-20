package com.entropy.arena.core.mixininterfaces;

import org.jetbrains.annotations.Nullable;

public interface LoadedConfigInterface {
    default @Nullable String entropyArena$getModID() {
        return null;
    }
}
