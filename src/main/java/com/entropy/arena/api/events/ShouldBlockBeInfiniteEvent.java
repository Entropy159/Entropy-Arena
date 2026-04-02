package com.entropy.arena.api.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;

public class ShouldBlockBeInfiniteEvent extends Event {
    private final Block block;
    private final ItemStack blockStack;
    private final LivingEntity entity;
    private boolean isInfinite = false;

    public ShouldBlockBeInfiniteEvent(Block block, ItemStack blockStack, LivingEntity entity) {
        this.block = block;
        this.blockStack = blockStack;
        this.entity = entity;
    }

    public Block getBlock() {
        return block;
    }

    public ItemStack getBlockStack() {
        return blockStack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    public void setInfinite(boolean infinite) {
        isInfinite = infinite;
    }
}
