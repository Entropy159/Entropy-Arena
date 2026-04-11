package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.events.IgnoreAdventureModeEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class ShowOutlineMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @ModifyExpressionValue(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean showWhenLookingAtTeamBlock(boolean original, @Local BlockPos blockpos, @Local BlockState blockState, @Local ItemStack itemstack) {
        if (!original && minecraft.getCameraEntity() instanceof Player player) {
            IgnoreAdventureModeEvent placing = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent(player, blockpos, blockState, itemstack, true));
            IgnoreAdventureModeEvent breaking = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent(player, blockpos, blockState, itemstack, false));
            return placing.shouldBypass() || breaking.shouldBypass();
        }
        return original;
    }
}
