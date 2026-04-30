package com.entropy.arena.core.mixin.team;

import com.entropy.arena.api.ArenaTeam;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    @Nullable
    public abstract PlayerTeam getTeam();

    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int correctColor(int original) {
        ArenaTeam team = ArenaTeam.fromTeam(getTeam());
        if (team != null) {
            return team.getColor();
        }
        return original;
    }
}
