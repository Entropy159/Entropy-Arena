package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.block.IgnoresAdventureMode;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @ModifyReturnValue(method = "canPlaceOnBlockInAdventureMode", at = @At("RETURN"))
    private boolean allowPlacingTeamBlocks(boolean original, @Local(argsOnly = true) BlockInWorld block) {
        return original || (getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IgnoresAdventureMode ignores && ignores.shouldIgnorePlace(block.getPos(), block.getState()));
    }

    @ModifyReturnValue(method = "canBreakBlockInAdventureMode", at = @At("RETURN"))
    private boolean allowBreakingTeamBlocks(boolean original, @Local(argsOnly = true) BlockInWorld block) {
        return original || (block.getState().getBlock() instanceof IgnoresAdventureMode ignores && ignores.shouldIgnoreBreak(block.getPos(), block.getState()));
    }
}
