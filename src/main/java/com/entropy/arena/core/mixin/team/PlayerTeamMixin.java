package com.entropy.arena.core.mixin.team;

import com.entropy.arena.api.ArenaTeam;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
    @Redirect(method = "getFormattedDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent formatDisplayName(MutableComponent instance, ChatFormatting format) {
        ArenaTeam team = ArenaTeam.fromTeam((PlayerTeam) (Object) this);
        if (team != null) {
            return instance.withColor(team.getColor());
        }
        return instance.withStyle(format);
    }

    @Redirect(method = "getFormattedName", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent formattedName(MutableComponent instance, ChatFormatting format) {
        ArenaTeam team = ArenaTeam.fromTeam((PlayerTeam) (Object) this);
        if (team != null) {
            return instance.withColor(team.getColor());
        }
        return instance.withStyle(format);
    }

    @ModifyReturnValue(method = "getColor", at = @At("RETURN"))
    private ChatFormatting color(ChatFormatting original) {
        return ChatFormatting.BLACK;
    }
}
