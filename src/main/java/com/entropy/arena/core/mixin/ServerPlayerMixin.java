package com.entropy.arena.core.mixin;

import com.entropy.arena.api.data.ArenaData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Shadow
    public abstract ServerLevel serverLevel();

    @ModifyReturnValue(method = "findRespawnAndUseSpawnBlock", at = @At("RETURN"))
    private static Optional<ServerPlayer.RespawnPosAngle> fix(Optional<ServerPlayer.RespawnPosAngle> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) float angle) {
        if (ArenaData.get(level).inGame() && original.isEmpty()) {
            return Optional.of(new ServerPlayer.RespawnPosAngle(pos.getBottomCenter(), angle));
        }
        return original;
    }

    @ModifyReturnValue(method = "getRespawnPosition", at = @At("RETURN"))
    private BlockPos respawnPos(BlockPos original) {
        if (!ArenaData.get(serverLevel()).inGame()) {
            GlobalPos lobbyPos = ArenaData.get(serverLevel()).lobbyPos;
            if (lobbyPos != null) {
                return lobbyPos.pos();
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getRespawnDimension", at = @At("RETURN"))
    private ResourceKey<Level> respawnDim(ResourceKey<Level> original) {
        if (!ArenaData.get(serverLevel()).inGame()) {
            GlobalPos lobbyPos = ArenaData.get(serverLevel()).lobbyPos;
            if (lobbyPos != null) {
                return lobbyPos.dimension();
            }
        }
        return original;
    }
}
