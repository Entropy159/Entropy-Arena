package com.entropy.arena.api.events;

import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired to check if a component should be propagated to an ItemStack from an ItemList when giving a loadout.
 * If canceled, then the component is not given.
 */
public class LoadoutComponentEvent extends Event implements ICancellableEvent {
    private final TypedDataComponent<?> component;
    private final ItemStack stack;

    public LoadoutComponentEvent(TypedDataComponent<?> component, ItemStack stack) {
        this.component = component;
        this.stack = stack;
    }

    public TypedDataComponent<?> getComponent() {
        return component;
    }

    public ItemStack getStack() {
        return stack;
    }
}
