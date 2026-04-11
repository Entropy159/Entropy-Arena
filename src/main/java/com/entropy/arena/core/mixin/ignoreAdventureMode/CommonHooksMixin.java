package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.events.IgnoreAdventureModeEvent;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
    @Definition(id = "player", local = @Local(type = Player.class, name = "player"))
    @Definition(id = "getAbilities", method = "Lnet/minecraft/world/entity/player/Player;getAbilities()Lnet/minecraft/world/entity/player/Abilities;")
    @Definition(id = "mayBuild", field = "Lnet/minecraft/world/entity/player/Abilities;mayBuild:Z")
    @Expression("player.getAbilities().mayBuild")
    @ModifyExpressionValue(method = "onPlaceItemIntoWorld", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean allowInfinite(boolean original, @Local(argsOnly = true) UseOnContext context, @Local(name = "itemstack") ItemStack itemstack) {
        IgnoreAdventureModeEvent event = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent(context.getPlayer(), context.getClickedPos(), context.getLevel().getBlockState(context.getClickedPos()), itemstack, true));
        return original || event.shouldBypass();
    }
}
