package com.entropy.arena.core.mixin.carpet;

import carpet.patches.EntityPlayerMPFake;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {
    @Redirect(method = "die", at = @At(value = "INVOKE", target = "Lcarpet/patches/EntityPlayerMPFake;kill(Lnet/minecraft/network/chat/Component;)V"))
    private void preventLeave(EntityPlayerMPFake instance, Component reason) {
        if (ArenaData.get(instance.serverLevel()).running) {
            EntropyArena.LOGGER.info("Prevented fake player {} from leaving on death!", instance.getScoreboardName());
        } else {
            instance.kill(reason);
        }
    }
}
