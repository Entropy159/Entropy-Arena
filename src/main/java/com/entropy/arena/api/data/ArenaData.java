package com.entropy.arena.api.data;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.MapList;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ArenaData extends SavedData {
    public boolean running = false;
    public boolean lobby = true;
    public int timer = 0;
    public boolean isTimed = true;
    public @Nullable BlockPos lobbyPos;
    public ArenaMap currentMap;
    public MapList mapList = new MapList();
    public ArenaGamemode currentGamemode;
    public HashMap<String, Loadout> loadouts = new HashMap<>();
    public HashMap<UUID, String> loadoutSelections = new HashMap<>();
    public HashMap<String, ItemList> itemLists = new HashMap<>();
    public final HashMap<UUID, String> mapVotes = new HashMap<>();
    public final HashMap<UUID, Boolean> typeVotes = new HashMap<>();
    public final ArrayList<String> votableMaps = new ArrayList<>();
    public final HashMap<UUID, Long> respawnTimes = new HashMap<>();

    public static ArenaData load(CompoundTag tag, HolderLookup.Provider provider) {
        ArenaData data = new ArenaData();
        data.mapList.loadFromTag(tag.getCompound("mapList"));
        data.loadouts = ArenaUtils.tagToHashMap(tag.getCompound("loadouts"), s -> s, t -> new Loadout((CompoundTag) t));
        data.itemLists = ArenaUtils.tagToHashMap(tag.getCompound("itemLists"), s -> s, t -> new ItemList((CompoundTag) t, provider));
        if (tag.contains("lobbyPos")) data.lobbyPos = BlockPos.of(tag.getLong("lobbyPos"));
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.put("mapList", mapList.saveToTag());
        tag.put("loadouts", ArenaUtils.mapToTag(loadouts, s -> s, Loadout::toTag));
        tag.put("itemLists", ArenaUtils.mapToTag(itemLists, s -> s, itemList -> itemList.toTag(registries)));
        if (lobbyPos != null) tag.putLong("lobbyPos", lobbyPos.asLong());
        return tag;
    }

    public static ArenaData get(ServerLevel level) {
        ArenaData data = level.getDataStorage().computeIfAbsent(new Factory<>(ArenaData::new, ArenaData::load), EntropyArena.MODID);
        data.setDirty();
        return data;
    }

    public void setLoadoutChoice(ServerPlayer player, String name) {
        if (loadouts.containsKey(name)) {
            loadoutSelections.put(player.getUUID(), name);
        }
    }

    public boolean inGame() {
        return running && !lobby && currentMap != null && currentGamemode != null;
    }
}
