package dev.entropy159.arena.api.map;

import dev.entropy159.entropylib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapList {
    private List<ArenaMap> maps = new ArrayList<>();

    public ListTag saveToTag() {
        return Utils.listToTag(maps, ArenaMap::toTag);
    }

    public void loadFromTag(ListTag tag) {
        maps = Utils.tagToArrayList(tag, t -> ArenaMap.fromTag((CompoundTag) t));
    }

    public boolean mapListIsEmpty() {
        return maps.isEmpty();
    }

    public @Nullable Component addMap(ServerLevel level, String name, ResourceLocation gamemode, BlockPos one, BlockPos two) {
        if (getMap(name) != null) {
            return Component.translatable("error.arena.map_already_exists", name);
        }
        ArenaMap map = new ArenaMap(level, name, gamemode, one, two);
        Component failureMessage = map.validate(level);
        if (failureMessage == null) maps.add(map);
        return failureMessage;
    }

    public void replaceMap(ArenaMap map) {
        if (getMap(map.getName()) != null) {
            maps.remove(getMap(map.getName()));
        }
        maps.add(map);
    }

    public boolean removeMap(String name) {
        return maps.remove(getMap(name));
    }

    public void forEachMap(Consumer<ArenaMap> function) {
        maps.forEach(function);
    }

    public List<ArenaMap> getAllMaps() {
        return new ArrayList<>(maps);
    }

    public @Nullable ArenaMap getMap(String name) {
        return maps.stream().filter(map -> map.getName().equals(name)).findFirst().orElse(null);
    }

    public ArrayList<ArenaMap> getEnabledMaps() {
        return new ArrayList<>(maps.stream().filter(ArenaMap::isEnabled).toList());
    }

    public ArrayList<ArenaMap> getValidMaps() {
        return new ArrayList<>(maps.stream().filter(ArenaMap::isValid).toList());
    }
}
