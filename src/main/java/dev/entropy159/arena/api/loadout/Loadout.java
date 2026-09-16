package dev.entropy159.arena.api.loadout;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.core.registry.ArenaDataComponents;
import dev.entropy159.entropylib.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Loadout implements IConfigurable {
    public static final StreamCodec<ByteBuf, Loadout> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, Loadout::isEnabled, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Loadout::getTags, Loadout::new);

    @Configurable(name = "Enabled")
    private boolean enabled = true;
    @Configurable(name = "Tags")
    private List<String> tags;
    private final CompoundTag gear;
    private final List<String> itemLists;

    public Loadout(CompoundTag tag) {
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        gear = tag.getCompound("gear");
        itemLists = tag.getList("itemLists", Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
        tags = Utils.tagToArrayList(tag.getList("tags", Tag.TAG_STRING), Tag::getAsString);
    }

    public Loadout(ServerPlayer player) {
        gear = LoadoutSerializerRegistry.serializeWithAll(player);
        itemLists = new ArrayList<>();
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.has(ArenaDataComponents.ITEM_LIST)) {
                itemLists.add(stack.get(ArenaDataComponents.ITEM_LIST));
            }
        });
        tags = new ArrayList<>(List.of("global"));
    }

    private Loadout(boolean enabled, List<String> tags) {
        this.enabled = enabled;
        this.tags = tags;
        gear = new CompoundTag();
        itemLists = new ArrayList<>();
    }

    public void giveToPlayer(ServerPlayer player) {
        LoadoutSerializerRegistry.deserializeWithAll(player, gear);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", enabled);
        tag.put("gear", gear);
        ListTag itemListsTag = new ListTag();
        itemLists.forEach(list -> itemListsTag.add(StringTag.valueOf(list)));
        tag.put("itemLists", itemListsTag);
        tag.put("tags", Utils.listToTag(tags, StringTag::valueOf));
        return tag;
    }

    public List<ItemList> getItemLists(ServerLevel level) {
        return itemLists.stream().map(name -> ArenaData.get(level).itemLists.get(name)).filter(Objects::nonNull).toList();
    }

    public boolean contains(ServerLevel level, Predicate<ItemStack> filter) {
        return LoadoutSerializerRegistry.contains(level, gear, filter);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void updateFrom(Loadout loadout) {
        enabled = loadout.enabled;
        tags = loadout.tags;
    }

    public enum TagMode {
        ANY,
        ALL
    }
}
