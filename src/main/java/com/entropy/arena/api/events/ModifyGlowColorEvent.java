package com.entropy.arena.api.events;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityEvent;

/**
 * Fires on the client to modify the glow color of an entity.
 */
public class ModifyGlowColorEvent extends EntityEvent {
    private int color;

    public ModifyGlowColorEvent(Entity entity, int color) {
        super(entity);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
