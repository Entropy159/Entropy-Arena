package dev.entropy159.arena.core.registry;

import dev.entropy159.arena.api.randomizer.ComponentRandomizer;
import dev.entropy159.arena.api.randomizer.ItemRandomizerRegistry;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Random;

public class ArenaItemRandomizers {
    public static void init() {
        ItemRandomizerRegistry.addRandomizer(EntropyArena.id("enchantment"), ComponentRandomizer.fromRange(DataComponents.ENCHANTMENTS, ctx -> ctx.player().level().registryAccess().registry(Registries.ENCHANTMENT).map(registry -> registry.holders().filter(ctx.stack()::supportsEnchantment).map(enchant -> {
            var enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchants.set(enchant, new Random().nextInt(enchant.value().getMaxLevel()) + 1);
            return enchants.toImmutable();
        }).toList()).orElse(null)));

        ItemRandomizerRegistry.addRandomizer(EntropyArena.id("durability"), new ComponentRandomizer<>(DataComponents.DAMAGE, ctx -> new Random().nextInt(ctx.stack().getMaxDamage()) + 1));
    }
}
