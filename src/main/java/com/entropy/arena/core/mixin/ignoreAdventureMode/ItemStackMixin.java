package com.entropy.arena.core.mixin.ignoreAdventureMode;

import com.entropy.arena.api.events.IgnoreAdventureModeEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @ModifyReturnValue(method = "canPlaceOnBlockInAdventureMode", at = @At("RETURN"))
    private boolean allowPlacingTeamBlocks(boolean original, @Local(argsOnly = true) BlockInWorld block) {
        IgnoreAdventureModeEvent event = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent(null, block.getPos(), block.getState(), (ItemStack) (Object) this, true));
        return original || event.shouldBypass();
    }

    @ModifyReturnValue(method = "canBreakBlockInAdventureMode", at = @At("RETURN"))
    private boolean allowBreakingTeamBlocks(boolean original, @Local(argsOnly = true) BlockInWorld block) {
        IgnoreAdventureModeEvent event = NeoForge.EVENT_BUS.post(new IgnoreAdventureModeEvent(null, block.getPos(), block.getState(), (ItemStack) (Object) this, false));
        return original || event.shouldBypass();
    }
}
