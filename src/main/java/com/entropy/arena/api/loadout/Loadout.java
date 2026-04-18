package com.entropy.arena.api.loadout;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.registry.ArenaDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Loadout {
    private final CompoundTag gear;
    private final List<String> itemLists;

    public Loadout(CompoundTag tag) {
        gear = tag.getCompound("gear");
        itemLists = tag.getList("itemLists", Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
    }

    public Loadout(ServerPlayer player) {
        gear = LoadoutSerializerRegistry.serializeWithAll(player);
        itemLists = new ArrayList<>();
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.has(ArenaDataComponents.ITEM_LIST)) {
                itemLists.add(stack.get(ArenaDataComponents.ITEM_LIST));
            }
        });
    }

    public void giveToPlayer(ServerPlayer player) {
        LoadoutSerializerRegistry.deserializeWithAll(player, gear);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("gear", gear);
        ListTag itemListsTag = new ListTag();
        itemLists.forEach(list -> itemListsTag.addTag(0, StringTag.valueOf(list)));
        tag.put("itemLists", itemListsTag);
        return tag;
    }

    public List<ItemList> getItemLists(ServerLevel level) {
        return itemLists.stream().map(name -> ArenaData.get(level).itemLists.get(name)).filter(Objects::nonNull).toList();
    }

    public boolean contains(ServerLevel level, Predicate<ItemStack> filter) {
        return LoadoutSerializerRegistry.contains(level, gear, filter);
    }
}
