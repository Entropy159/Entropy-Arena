package dev.entropy159.arena.api.loadout;

import dev.entropy159.arena.core.registry.ArenaDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemList {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemList> STREAM_CODEC = StreamCodec.of((buf, val) -> {
        buf.writeUtf(val.name);
        buf.writeEnum(val.mode);
        boolean hasTag = val.tagKey != null;
        buf.writeBoolean(hasTag);
        if (hasTag) {
            buf.writeResourceLocation(val.tagKey.location());
        }
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, val.stacks);
    }, buf -> {
        String name = buf.readUtf();
        Mode mode = buf.readEnum(Mode.class);
        TagKey<Item> tagKey = null;
        if (buf.readBoolean()) {
            tagKey = TagKey.create(Registries.ITEM, buf.readResourceLocation());
        }
        List<ItemStack> stacks = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        return new ItemList(stacks, name, tagKey, mode);
    });

    private final String name;
    private List<ItemStack> stacks = new ArrayList<>();
    private @Nullable TagKey<Item> tagKey = null;
    private Mode mode = Mode.RANDOM;

    private ItemList(List<ItemStack> stacks, String name, @Nullable TagKey<Item> tagKey, Mode mode) {
        this.name = name;
        this.stacks = stacks;
        this.tagKey = tagKey;
        this.mode = mode;
    }

    public ItemList(String name, Mode mode, @Nullable TagKey<Item> tagKey) {
        this(new ArrayList<>(), name, tagKey, mode);
    }

    public ItemList(ServerLevel level, String name, BlockPos pos, Mode mode, @Nullable TagKey<Item> tag) {
        this.name = name;
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

    public ItemList(String name, CompoundTag tag, HolderLookup.Provider provider) {
        this.name = name;
        String modeName = tag.getString("mode").toUpperCase();
        mode = modeName.isBlank() ? Mode.RANDOM : Mode.valueOf(modeName);
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
        if (index >= size()) {
            return ItemStack.EMPTY;
        }
        return stacks.get(index).copy();
    }

    public ItemStack getRandom() {
        int index = 0;
        if (!stacks.isEmpty()) {
            index = new Random().nextInt(stacks.size());
        }
        return get(index);
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

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isTag() {
        return tagKey != null;
    }

    public ResourceLocation getTag() {
        return tagKey == null ? null : tagKey.location();
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

    public ItemStack getItem() {
        ItemStack stack = getRandom();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        stack.set(ArenaDataComponents.ITEM_LIST, name);
        return stack;
    }

    public void setStack(ItemStack stack, int index) {
        if (index >= stacks.size()) {
            stacks.add(stack);
        } else {
            stacks.set(index, stack);
        }
        stacks.removeIf(ItemStack::isEmpty);
    }

    public String getName() {
        return name;
    }

    public IItemHandlerModifiable getHandler() {
        return new StackHandler(this);
    }

    public enum Mode {
        BOTH,
        RANDOM,
        ORDERED
    }

    public static class StackHandler extends ItemStackHandler {
        private final ItemList list;

        public StackHandler(ItemList list) {
            super(list.size() + 1);
            this.list = list;
            for (int i = 0; i < list.size(); i++) {
                setStackInSlot(i, list.get(i));
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack stack = getStackInSlot(slot);
            list.setStack(stack.copy(), slot);
        }
    }
}