package com.entropy.arena.core.mixin;

import com.entropy.arena.api.events.ShouldBlockBeInfiniteEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow
    public abstract Block getBlock();

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void infiniteBlocks(ItemStack instance, int amount, LivingEntity entity) {
        if (!NeoForge.EVENT_BUS.post(new ShouldBlockBeInfiniteEvent(getBlock(), instance, entity)).isInfinite()) {
            instance.consume(amount, entity);
        }
    }
}
