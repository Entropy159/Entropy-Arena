package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.gamemodes.Disguise;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    public boolean disguiseGamemode$hasDisguise() {
        if (level().isClientSide()) {
            if (ClientData.running && !ClientData.inLobby && ClientData.currentGamemode instanceof Disguise gamemode) {
                BlockState disguise = gamemode.getDisguise((Player) (Object) this);
                return disguise != null;
            }
        } else if (level() instanceof ServerLevel serverLevel) {
            if (ArenaData.get(serverLevel).currentGamemode instanceof Disguise gamemode) {
                BlockState disguise = gamemode.getDisguise((Player) (Object) this);
                return disguise != null;
            }
        }
        return false;
    }

    @Override
    public boolean isInWall() {
        return super.isInWall() && !disguiseGamemode$hasDisguise();
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !disguiseGamemode$hasDisguise();
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !disguiseGamemode$hasDisguise();
    }

    @Override
    public void push(@NotNull Entity entity) {
        if (disguiseGamemode$hasDisguise()) {
            return;
        }
        super.push(entity);
    }

    @ModifyReturnValue(method = "getDefaultDimensions", at = @At("RETURN"))
    private EntityDimensions shrink(EntityDimensions original) {
        if (disguiseGamemode$hasDisguise()) {
            return EntityDimensions.fixed(1, 1);
        }
        return original;
    }
}
