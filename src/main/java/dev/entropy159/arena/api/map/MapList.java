package dev.entropy159.arena.api.map;

import dev.entropy159.entropylib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class MapList {
    private HashMap<String, ArenaMap> maps = new HashMap<>();

    public CompoundTag saveToTag() {
        return Utils.mapToTag(maps, s -> s, ArenaMap::toTag);
    }

    public void loadFromTag(CompoundTag tag) {
        maps = Utils.tagToHashMap(tag, s -> s, t -> ArenaMap.fromTag((CompoundTag) t));
    }

    public boolean mapListIsEmpty() {
        return maps.isEmpty();
    }

    public @Nullable Component addMap(ServerLevel level, String name, ResourceLocation gamemode, BlockPos one, BlockPos two) {
        if (maps.containsKey(name)) {
            return Component.translatable("error.arena.map_already_exists", name);
        }
        ArenaMap map = new ArenaMap(level, name, gamemode, one, two);
        Component failureMessage = map.validate(level);
        if (failureMessage == null) maps.put(name, map);
        return failureMessage;
    }

    public boolean removeMap(String name) {
        return maps.remove(name) != null;
    }

    public void forEachMap(Consumer<ArenaMap> function) {
        maps.values().forEach(function);
    }

    public @Nullable ArenaMap getMap(String name) {
        return maps.get(name);
    }

    public ArrayList<ArenaMap> getEnabledMaps() {
        return new ArrayList<>(maps.values().stream().filter(ArenaMap::isEnabled).toList());
    }

    public ArrayList<ArenaMap> getValidMaps() {
        return new ArrayList<>(maps.values().stream().filter(ArenaMap::isValid).toList());
    }
}
