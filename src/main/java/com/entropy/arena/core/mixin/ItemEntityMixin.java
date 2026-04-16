package com.entropy.arena.core.mixin;

import com.entropy.arena.api.events.ItemEntityExplosionEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow
    public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        TriState isImmune = NeoForge.EVENT_BUS.post(new ItemEntityExplosionEvent((ItemEntity) (Object) this)).isImmune();
        if (!isImmune.isDefault()) {
            return isImmune.isTrue();
        }
        return super.ignoreExplosion(explosion);
    }
}
