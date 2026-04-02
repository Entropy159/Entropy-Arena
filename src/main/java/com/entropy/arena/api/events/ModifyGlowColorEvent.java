package com.entropy.arena.api.events;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityEvent;

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
