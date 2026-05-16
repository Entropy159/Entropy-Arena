package com.entropy.arena.api.registrate;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class GamemodeBuilder<T extends ArenaGamemode, P> extends AbstractBuilder<ArenaGamemode, T, P, GamemodeBuilder<T, P>> {
    private final NonNullFunction<ResourceLocation, T> factory;
    private final ResourceLocation id;

    public static <T extends ArenaGamemode, P> GamemodeBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<ResourceLocation, T> factory) {
        return new GamemodeBuilder<>(owner, parent, name, callback, factory).defaultLang();
    }

    protected GamemodeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<ResourceLocation, T> factory) {
        super(owner, parent, name, callback, GamemodeRegistry.REGISTRY_KEY);
        this.factory = factory;
        this.id = ResourceLocation.fromNamespaceAndPath(owner.getModid(), name);
    }

    public GamemodeBuilder<T, P> defaultLang() {
        return lang(t -> "arena.gamemode." + t.getRegistryID().toLanguageKey());
    }

    public GamemodeBuilder<T, P> lang(String name) {
        return lang(t -> "arena.gamemode." + t.getRegistryID().toLanguageKey(), name);
    }

    @Override
    protected @NotNull T createEntry() {
        return factory.apply(id);
    }

    @Override
    protected @NotNull RegistryEntry<ArenaGamemode, T> createEntryWrapper(@NotNull DeferredHolder<ArenaGamemode, T> delegate) {
        return new GamemodeEntry<>(getOwner(), delegate);
    }

    @Override
    public @NotNull GamemodeEntry<T> register() {
        return (GamemodeEntry<T>) super.register();
    }
}
