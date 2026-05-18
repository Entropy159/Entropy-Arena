package com.entropy.arena.api.registrate;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class ArenaRegistrate extends AbstractRegistrate<ArenaRegistrate> {
    protected ArenaRegistrate(String modid) {
        super(modid);
    }

    public static ArenaRegistrate create(String modID) {
        ArenaRegistrate registrate = new ArenaRegistrate(modID);
        registrate.defaultCreativeTab(CreativeModeTabs.OP_BLOCKS);
        ModList.get().getModContainerById(modID).map(ModContainer::getEventBus).ifPresentOrElse(registrate::registerEventListeners, () -> LogUtils.getLogger().error("Failed to register mod event bus for ArenaRegistrate of mod ID {}", modID));
        return registrate;
    }

    public <T extends ArenaGamemode> GamemodeBuilder<T, ArenaRegistrate> gamemode(NonNullFunction<ResourceLocation, T> factory) {
        return gamemode(self(), factory);
    }

    public <T extends ArenaGamemode> GamemodeBuilder<T, ArenaRegistrate> gamemode(String name, NonNullFunction<ResourceLocation, T> factory) {
        return gamemode(self(), name, factory);
    }

    public <T extends ArenaGamemode, P> GamemodeBuilder<T, P> gamemode(P parent, NonNullFunction<ResourceLocation, T> factory) {
        return gamemode(parent, currentName(), factory);
    }

    public <T extends ArenaGamemode, P> GamemodeBuilder<T, P> gamemode(P parent, String name, NonNullFunction<ResourceLocation, T> factory) {
        return entry(name, callback -> GamemodeBuilder.create(this, parent, name, callback, factory));
    }
}
