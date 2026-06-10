package com.entropy.arena.api.data;

import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.ArenaMapBackup;
import com.entropy.arena.api.map.MapList;
import com.entropy.arena.api.util.ArenaGameType;
import com.entropy.arena.api.util.ArenaUtils;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ArenaData extends SavedData {
    private final MinecraftServer server;

    public ArenaMapBackup.BackupState backupState = ArenaMapBackup.BackupState.NO_BACKUP;
    public boolean running = false;
    public boolean lobby = true;
    public int timer = 0;
    public ArenaGameType gameType = ArenaGameType.TIMED;
    public @Nullable GlobalPos lobbyPos;
    public ArenaMap currentMap;
    public MapList mapList = new MapList();
    public ArenaGamemode currentGamemode;
    public HashMap<String, Loadout> loadouts = new HashMap<>();
    public HashMap<UUID, String> loadoutSelections = new HashMap<>();
    public HashMap<String, ItemList> itemLists = new HashMap<>();
    public final HashMap<UUID, String> mapVotes = new HashMap<>();
    public final HashMap<UUID, ArenaGameType> typeVotes = new HashMap<>();
    public final ArrayList<String> votableMaps = new ArrayList<>();
    public final HashMap<UUID, Long> respawnTimes = new HashMap<>();
    public final HashMap<UUID, Long> spawnProtection = new HashMap<>();
    public final HashMap<UUID, Long> teamSwitchTimes = new HashMap<>();

    public ArenaData(MinecraftServer server) {
        this.server = server;
    }

    public static ArenaData load(MinecraftServer server, CompoundTag tag, HolderLookup.Provider provider) {
        ArenaData data = new ArenaData(server);
        if (tag.contains("backupState")) {
            data.backupState = ArenaMapBackup.BackupState.valueOf(tag.getString("backupState"));
            if (data.backupState != ArenaMapBackup.BackupState.NO_BACKUP) {
                data.restoreBackup();
            }
        } else {
            data.backupState = ArenaMapBackup.BackupState.NO_BACKUP;
        }
        data.mapList.loadFromTag(tag.getCompound("mapList"));
        data.loadouts = ArenaUtils.tagToHashMap(tag.getCompound("loadouts"), s -> s, t -> new Loadout((CompoundTag) t));
        data.itemLists = ArenaUtils.tagToHashMap(tag.getCompound("itemLists"), s -> s, t -> new ItemList((CompoundTag) t, provider));
        if (tag.contains("lobbyPos", CompoundTag.TAG_COMPOUND)) {
            CompoundTag lobbyTag = tag.getCompound("lobbyPos");
            data.lobbyPos = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(lobbyTag.getString("dimension"))), BlockPos.of(lobbyTag.getLong("pos")));
        } else if (tag.contains("lobbyPos", Tag.TAG_LONG)) {
            data.lobbyPos = GlobalPos.of(Level.OVERWORLD, BlockPos.of(tag.getLong("lobbyPos")));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.putString("backupState", backupState.name());
        tag.put("mapList", mapList.saveToTag());
        tag.put("loadouts", ArenaUtils.mapToTag(loadouts, s -> s, Loadout::toTag));
        tag.put("itemLists", ArenaUtils.mapToTag(itemLists, s -> s, itemList -> itemList.toTag(registries)));
        if (lobbyPos != null) {
            CompoundTag lobbyTag = new CompoundTag();
            lobbyTag.putString("dimension", lobbyPos.dimension().location().toString());
            lobbyTag.putLong("pos", lobbyPos.pos().asLong());
            tag.put("lobbyPos", lobbyTag);
        }
        return tag;
    }

    public static ArenaData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static ArenaData get(MinecraftServer server) {
        ArenaData data = server.overworld().getDataStorage().computeIfAbsent(new Factory<>(() -> new ArenaData(server), (tag, registries) -> ArenaData.load(server, tag, registries)), EntropyArena.MODID);
        data.setDirty();
        return data;
    }

    public String setLoadoutChoice(ServerPlayer player, String name) {
        if (loadouts.containsKey(name)) {
            return loadoutSelections.put(player.getUUID(), name);
        }
        return null;
    }

    public boolean inGame() {
        return running && !lobby && currentMap != null && currentGamemode != null;
    }

    public void backup(Runnable after) {
        if (currentGamemode != null) {
            backupState = ArenaMapBackup.BackupState.BACKING_UP;
            ArenaMapBackup.backup(server, currentMap, currentGamemode.getPropertiesToLookFor(), () -> {
                backupState = ArenaMapBackup.BackupState.HAS_BACKUP;
                after.run();
            });
        }
    }

    public void restoreBackup() {
        restoreBackup(() -> {
        });
    }

    public void restoreBackup(Runnable after) {
        backupState = ArenaMapBackup.BackupState.RESTORING;
        ArenaMapBackup.restore(server, () -> {
            backupState = ArenaMapBackup.BackupState.NO_BACKUP;
            after.run();
        });
    }
}
