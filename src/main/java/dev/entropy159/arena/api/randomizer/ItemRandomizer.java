package dev.entropy159.arena.api.randomizer;

import dev.entropy159.arena.api.loadout.LoadoutSerializer;
import dev.entropy159.arena.core.registry.ArenaDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public abstract class ItemRandomizer {
    protected ResourceLocation id;

    public MutableComponent getName() {
        return Component.translatable(getNameKey());
    }

    public String getNameKey() {
        return "randomizer." + id.toLanguageKey();
    }

    void setID(ResourceLocation id) {
        this.id = id;
    }

    public abstract void randomize(Context context);

    public void apply(ItemStack stack) {
        var randomizers = new ArrayList<>(stack.getOrDefault(ArenaDataComponents.ITEM_RANDOMIZERS, new ArrayList<>()));
        randomizers.add(id);
        stack.set(ArenaDataComponents.ITEM_RANDOMIZERS, randomizers);
    }

    public void remove(ItemStack stack) {
        var randomizers = new ArrayList<>(stack.getOrDefault(ArenaDataComponents.ITEM_RANDOMIZERS, new ArrayList<>()));
        randomizers.remove(id);
        stack.set(ArenaDataComponents.ITEM_RANDOMIZERS, randomizers);
    }

    public record Context(ServerPlayer player, ItemStack stack, int slot, LoadoutSerializer serializer) {
    }
}
