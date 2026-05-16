package com.entropy.arena.api.registrate;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GamemodeEntry<T extends ArenaGamemode> extends RegistryEntry<ArenaGamemode, T> {
    public GamemodeEntry(AbstractRegistrate<?> owner, DeferredHolder<ArenaGamemode, T> key) {
        super(owner, key);
    }
}
