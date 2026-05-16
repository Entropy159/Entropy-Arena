package com.entropy.arena.api;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class ArenaRegistrate extends AbstractRegistrate<ArenaRegistrate> {
    protected ArenaRegistrate(String modid) {
        super(modid);
    }

    public static ArenaRegistrate create(String modID) {
        ArenaRegistrate registrate = new ArenaRegistrate(modID);
        ModList.get().getModContainerById(modID).map(ModContainer::getEventBus).ifPresentOrElse(registrate::registerEventListeners, () -> LogUtils.getLogger().error("Failed to register mod event bus for ArenaRegistrate of mod ID {}", modID));
        return registrate;
    }
}
