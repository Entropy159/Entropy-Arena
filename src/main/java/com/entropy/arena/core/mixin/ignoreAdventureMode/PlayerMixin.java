package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.block.IgnoresAdventureMode;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyReturnValue(method = "blockActionRestricted", at = @At("TAIL"))
    private boolean allowBreakingTeamBlocks(boolean original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        return original && !(level.getBlockState(pos).getBlock() instanceof IgnoresAdventureMode ignores && ignores.shouldIgnoreBreak(pos, level.getBlockState(pos)));
    }
}
