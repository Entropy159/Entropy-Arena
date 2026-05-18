package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Redirect(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V"))
    private void print(Entity instance, double x, double y, double z, float yRot, float xRot, int steps) {
        if (instance instanceof Player && ClientData.running) {
            instance.absMoveTo(x, y, z, yRot, xRot);
        } else {
            instance.lerpTo(x, y, z, yRot, xRot, steps);
        }
    }
}
