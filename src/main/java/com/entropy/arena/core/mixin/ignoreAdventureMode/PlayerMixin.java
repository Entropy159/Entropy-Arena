package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.events.IgnoreAdventureModeEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyReturnValue(method = "blockActionRestricted", at = @At("TAIL"))
    private boolean allowBreakingTeamBlocks(boolean original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Local ItemStack stack) {
        IgnoreAdventureModeEvent event = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent((Player) (Object) this, pos, level.getBlockState(pos), stack, false));
        return original && !event.shouldBypass();
    }
}
