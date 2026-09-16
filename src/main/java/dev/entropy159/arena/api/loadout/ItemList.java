package dev.entropy159.arena.api.loadout;

import dev.entropy159.arena.core.registry.ArenaDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemList {
    private final ArrayList<ItemStack> stacks = new ArrayList<>();
    private @Nullable TagKey<Item> tagKey = null;
    private Mode mode = Mode.RANDOM;

    public ItemList(ServerLevel level, BlockPos pos, Mode mode, @Nullable TagKey<Item> tag) {
        if (tag != null) {
            tagKey = tag;
        } else {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                saveFromBlock(handler);
            }
        }
        this.mode = mode;
    }

    public ItemList(CompoundTag tag, HolderLookup.Provider provider) {
        mode = Mode.valueOf(tag.getString("mode").toUpperCase());
        if (tag.contains("tagKey")) {
            tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tag.getString("tagKey")));
        } else {
            tag.getList("stacks", ListTag.TAG_COMPOUND).forEach(t -> ItemStack.parse(provider, t).ifPresent(stacks::add));
        }
    }

    public CompoundTag toTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("mode", mode.name().toLowerCase());
        if (tagKey != null) {
            tag.putString("tagKey", tagKey.location().toString());
        } else {
            ListTag list = new ListTag(ListTag.TAG_COMPOUND);
            stacks.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> list.addTag(list.size(), stack.save(provider)));
            tag.put("stacks", list);
        }
        return tag;
    }

    public ItemStack get(int index) {
        if (tagKey != null) {
            List<Holder<Item>> items = BuiltInRegistries.ITEM.getOrCreateTag(tagKey).stream().toList();
            return new ItemStack(items.get(new Random().nextInt(items.size())));
        }
        return stacks.get(index).copy();
    }

    public int size() {
        if (tagKey != null) {
            return BuiltInRegistries.ITEM.getOrCreateTag(tagKey).size();
        }
        return stacks.size();
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isTag() {
        return tagKey != null;
    }

    public void loadToBlock(IItemHandler handler) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            handler.insertItem(slot, stacks.get(slot).copy(), false);
        }
    }

    public void saveFromBlock(IItemHandler handler) {
        stacks.clear();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) stacks.add(handler.getStackInSlot(slot).copy());
        }
    }

    public ItemStack getItem(String name) {
        ItemStack stack = get(0);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        stack.set(ArenaDataComponents.ITEM_LIST, name);
        return stack;
    }

    public enum Mode {
        BOTH,
        RANDOM,
        ORDERED
    }
}