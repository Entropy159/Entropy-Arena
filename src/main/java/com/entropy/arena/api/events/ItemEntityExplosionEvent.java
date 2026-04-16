package com.entropy.arena.api.events;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.TriState;

public class ItemEntityExplosionEvent extends Event {
    private final ItemEntity entity;
    private TriState isImmune = TriState.DEFAULT;

    public ItemEntityExplosionEvent(ItemEntity itemEntity) {
        entity = itemEntity;
    }

    public ItemEntity getEntity() {
        return entity;
    }

    public TriState isImmune() {
        return isImmune;
    }

    public void setImmune(TriState immune) {
        isImmune = immune;
    }
}
