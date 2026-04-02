package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.block.IgnoresAdventureMode;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class ShowOutlineMixin {
    @ModifyExpressionValue(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean showWhenLookingAtTeamBlock(boolean original, @Local BlockPos blockpos, @Local BlockState blockState) {
        return original && !(blockState.getBlock() instanceof IgnoresAdventureMode ignores && ignores.shouldIgnoreBreak(blockpos, blockState));
    }
}
