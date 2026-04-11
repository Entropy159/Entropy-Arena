package com.entropy.arena.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

public class IgnoreAdventureModeEvent extends Event {
    private final BlockPos pos;
    private final BlockState state;
    private final ItemStack heldItem;
    private final @Nullable Player player;
    private final boolean isPlacing;
    private boolean bypass = false;

    public IgnoreAdventureModeEvent(@Nullable Player player, BlockPos pos, BlockState state, ItemStack heldItem, boolean isPlacing) {
        this.player = player;
        this.pos = pos;
        this.state = state;
        this.heldItem = heldItem;
        this.isPlacing = isPlacing;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public boolean isPlacing() {
        return isPlacing;
    }

    public boolean shouldBypass() {
        return bypass;
    }

    public void setBypass(boolean shouldBypass) {
        bypass = shouldBypass;
    }
}
