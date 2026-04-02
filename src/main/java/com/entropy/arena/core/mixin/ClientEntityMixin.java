package com.entropy.arena.core.mixin;

import com.entropy.arena.api.events.ModifyGlowColorEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class ClientEntityMixin {
    @Shadow
    public abstract int getId();

    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int customColor(int original) {
        return NeoForge.EVENT_BUS.post(new ModifyGlowColorEvent((Entity) (Object) this, original)).getColor();
    }
}
