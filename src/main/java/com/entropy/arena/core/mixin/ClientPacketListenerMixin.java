package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Redirect(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFI)V"))
    private void unlerp(Entity instance, double x, double y, double z, float yRot, float xRot, int steps) {
        Vec3 unlerpPos = ClientData.entitiesToUnlerp.get(instance.getId());
        if (unlerpPos != null && BlockPos.containing(unlerpPos).equals(BlockPos.containing(x, y, z))) {
            ClientData.entitiesToUnlerp.remove(instance.getId());
            instance.moveTo(x, y, z, yRot % 360, xRot % 360);
            instance.lerpTo(x, y, z, yRot, xRot, 0);
        } else {
            instance.lerpTo(x, y, z, yRot, xRot, steps);
        }
    }
}
