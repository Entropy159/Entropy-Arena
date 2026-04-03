package com.entropy.arena.core.map;

import com.entropy.arena.api.ArenaUtils;
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
    private static HashMap<String, ArenaMap> maps = new HashMap<>();

    public static CompoundTag saveToTag() {
        return ArenaUtils.mapToTag(maps, s -> s, ArenaMap::toTag);
    }

    public static void loadFromTag(CompoundTag tag) {
        maps = ArenaUtils.tagToHashMap(tag, s -> s, t -> ArenaMap.fromTag((CompoundTag) t));
    }

    public static boolean mapListIsEmpty() {
        return maps.isEmpty();
    }

    public static @Nullable Component addMap(ServerLevel level, String name, ResourceLocation gamemode, BlockPos one, BlockPos two) {
        if (maps.containsKey(name)) {
            return Component.translatable("arena.error.map_already_exists", name);
        }
        ArenaMap map = new ArenaMap(level, name, gamemode, one, two);
        Component failureMessage = map.validate(level);
        if (failureMessage == null) maps.put(name, map);
        return failureMessage;
    }

    public static boolean removeMap(String name) {
        return maps.remove(name) != null;
    }

    public static void forEachMap(Consumer<ArenaMap> function) {
        maps.values().forEach(function);
    }

    public static @Nullable ArenaMap getMap(String name) {
        return maps.get(name);
    }

    public static ArrayList<ArenaMap> getMaps() {
        return new ArrayList<>(maps.values().stream().toList());
    }
}
