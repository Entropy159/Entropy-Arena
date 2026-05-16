package com.entropy.arena.api.registrate;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class GamemodeEntry<T extends Supplier<ArenaGamemode>> extends RegistryEntry<Supplier<ArenaGamemode>, T> {
    public GamemodeEntry(AbstractRegistrate<?> owner, DeferredHolder<Supplier<ArenaGamemode>, T> key) {
        super(owner, key);
    }
}
