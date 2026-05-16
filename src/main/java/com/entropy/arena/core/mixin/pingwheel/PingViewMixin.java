package com.entropy.arena.core.mixin.pingwheel;

import com.entropy.arena.api.ArenaTeam;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import nx.pingwheel.common.core.PingView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PingView.class)
public class PingViewMixin {
    @Shadow
    private @Nullable PlayerInfo playerInfo;

    @Definition(id = "getColor", method = "Lnet/minecraft/world/scores/PlayerTeam;getColor()Lnet/minecraft/ChatFormatting;")
    @Definition(id = "getColor2", method = "Lnet/minecraft/ChatFormatting;getColor()Ljava/lang/Integer;")
    @Definition(id = "playerInfo", field = "Lnx/pingwheel/common/core/PingView;playerInfo:Lnet/minecraft/client/multiplayer/PlayerInfo;")
    @Definition(id = "getTeam", method = "Lnet/minecraft/client/multiplayer/PlayerInfo;getTeam()Lnet/minecraft/world/scores/PlayerTeam;")
    @Expression("this.playerInfo.getTeam().getColor().getColor2()")
    @Redirect(method = "getTeamColor", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Integer compat(ChatFormatting instance) {
        if (playerInfo == null) return instance.getColor();
        ArenaTeam team = ArenaTeam.fromTeam(playerInfo.getTeam());
        if (team != null) {
            return team.getColor();
        }
        return instance.getColor();
    }
}
