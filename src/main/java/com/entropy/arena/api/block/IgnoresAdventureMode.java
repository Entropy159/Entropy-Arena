package com.entropy.arena.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface IgnoresAdventureMode {
    default boolean shouldIgnorePlace(BlockPos placeOnPos, BlockState placeOnState) {
        return true;
    }

    default boolean shouldIgnoreBreak(BlockPos pos, BlockState state) {
        return true;
    }
}
