package com.entropy.arena.api.events;

import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Fired to check if a component should be propagated to an ItemStack from an ItemList when giving a loadout.
 */
public class LoadoutComponentEvent extends Event {
    private final TypedDataComponent<?> component;
    private final ItemStack stack;
    private boolean allowed = false;

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

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allow) {
        allowed = allow;
    }
}
