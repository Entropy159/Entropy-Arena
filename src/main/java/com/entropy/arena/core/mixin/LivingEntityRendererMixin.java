package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"))
    private boolean hideNametags(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        if (ClientData.running && !ClientData.inLobby && ClientData.currentGamemode != null && entity instanceof Player player) {
            return original && ClientData.currentGamemode.shouldShowNametag(player);
        }
        return original;
    }
}
