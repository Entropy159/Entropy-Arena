package com.entropy.arena.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implement this on a Block to allow it to bypass Adventure mode restrictions.
 * Methods are called on both the server and the client, do not access sided code without checks.
 */
public interface IgnoresAdventureMode {
    /**
     * Called to determine whether to bypass placement restrictions.
     *
     * @param placeOnPos The position of the block being placed on
     * @param placeOnState The block state being placed on
     * @return Whether to ignore the restrictions
     */
    default boolean shouldIgnorePlace(BlockPos placeOnPos, BlockState placeOnState) {
        return true;
    }

    /**
     * Called to determine whether to bypass breaking restrictions.
     *
     * @param pos The position of the block being broken
     * @param state The block state being broken
     * @return Whether to ignore the restrictions
     */
    default boolean shouldIgnoreBreak(BlockPos pos, BlockState state) {
        return true;
    }
}
