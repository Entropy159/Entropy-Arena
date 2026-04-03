package com.entropy.arena.api.data;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.events.GiveLoadoutEvent;
import com.entropy.arena.api.events.MatchEndEvent;
import com.entropy.arena.api.events.MatchStartEvent;
import com.entropy.arena.api.events.TeleportToLobbyEvent;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.map.ArenaMap;
import com.entropy.arena.core.map.ArenaMapInfo;
import com.entropy.arena.core.map.MapList;
import com.entropy.arena.core.network.toClient.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ArenaData extends SavedData {
    public static final int MAPS_FOR_VOTING = 3;

    private ServerLevel level;

    private boolean running = false;
    private boolean lobby = false;
    private int timer = 0;
    private int targetScore = 0;
    private @Nullable BlockPos lobbyPos;
    private ArenaMap currentMap;
    private ArenaGamemode currentGamemode;
    private HashMap<String, Loadout> loadouts = new HashMap<>();
    private HashMap<UUID, String> loadoutSelections = new HashMap<>();
    private final HashMap<UUID, String> mapVotes = new HashMap<>();
    private final ArrayList<String> votableMaps = new ArrayList<>();
    private final HashMap<UUID, Long> respawnTimes = new HashMap<>();

    public static ArenaData load(CompoundTag tag, HolderLookup.Provider provider) {
        MapList.loadFromTag(tag.getCompound("mapList"));
        ArenaData data = new ArenaData();
        data.targetScore = tag.getInt("targetScore");
        data.loadouts = ArenaUtils.tagToHashMap(tag.getCompound("loadouts"), s -> s, t -> new Loadout((CompoundTag) t));
        data.loadoutSelections = ArenaUtils.tagToHashMap(tag.getCompound("loadoutSelections"), UUID::fromString, Tag::getAsString);
        if (tag.contains("lobbyPos")) data.lobbyPos = BlockPos.of(tag.getLong("lobbyPos"));
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.put("mapList", MapList.saveToTag());
        tag.putInt("targetScore", targetScore);
        tag.put("loadouts", ArenaUtils.mapToTag(loadouts, s -> s, Loadout::getCompound));
        tag.put("loadoutSelections", ArenaUtils.mapToTag(loadoutSelections, UUID::toString, StringTag::valueOf));
        if (lobbyPos != null) tag.putLong("lobbyPos", lobbyPos.asLong());
        return tag;
    }

    public static ArenaData get(ServerLevel level) {
        ArenaData data = level.getDataStorage().computeIfAbsent(new Factory<>(ArenaData::new, ArenaData::load), EntropyArena.MODID);
        if (data.level == null) data.level = level;
        data.setDirty();
        return data;
    }

    public @Nullable Component enable() {
        if (running) {
            return Component.translatable("arena.error.already_running");
        }
        if (lobbyPos == null) {
            return Component.translatable("arena.error.no_lobby");
        }
        if (MapList.mapListIsEmpty()) {
            return Component.translatable("arena.error.no_maps");
        }
        if (loadouts.isEmpty()) {
            return Component.translatable("arena.error.no_loadouts");
        }
        Notification.toAll(Component.translatable("arena.message.enable").withStyle(ChatFormatting.DARK_GREEN));
        timer = ServerConfig.INTERVAL_SECONDS.get();
        lobby = true;
        running = true;
        sendAllToLobby();
        startMapVote();
        return null;
    }

    public void disable() {
        if (running) {
            running = false;
            timer = 0;
            onMatchEnd();
        }
    }

    public void setLobbyPos(@Nullable BlockPos pos) {
        lobbyPos = pos;
    }

    public void sendAllToLobby() {
        level.players().forEach(this::sendToLobby);
        RunningPacket.sendToEveryone(this);
    }

    private void sendToLobby(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Pre(this, player));
        if (lobbyPos == null) {
            return;
        }
        ArenaUtils.teleportToPos(player, lobbyPos);
        player.setGameMode(GameType.ADVENTURE);
        player.setGlowingTag(false);
        player.getInventory().clearContent();
        level.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Post(this, player));
    }

    public void onMatchEnd() {
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Pre(this));
        Notification.toAll(Component.translatable("arena.message.game_over").withStyle(ChatFormatting.RED));
        PacketDistributor.sendToAllPlayers(new ScoresPacket(List.of()));
        sendAllToLobby();
        if (getCurrentGamemode() != null) {
            getCurrentGamemode().onMatchEnd(this);
            currentGamemode = null;
        }
        if (currentMap != null) currentMap.reset(level);
        currentMap = null;
        if (running) {
            timer = ServerConfig.INTERVAL_SECONDS.get();
            PacketDistributor.sendToAllPlayers(new TimerPacket(timer));
            lobby = true;

            startMapVote();
        }
        RunningPacket.sendToEveryone(this);
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(this));
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Post(this));
    }

    private void startMapVote() {
        mapVotes.clear();
        ArrayList<ArenaMap> maps = MapList.getMaps();
        ArrayList<ArenaMapInfo> mapInfos = new ArrayList<>();
        int mapCount = Math.min(MAPS_FOR_VOTING, maps.size());
        for (int i = 0; i < mapCount; i++) {
            int mapIndex = new Random().nextInt(maps.size());
            ArenaMap map = maps.get(mapIndex);
            maps.remove(map);
            votableMaps.add(map.getName());
            mapInfos.add(map.getInfo(level));
        }
        PacketDistributor.sendToAllPlayers(new VotableMapsPacket(mapInfos));
    }

    public void onMatchStart() {
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Pre(this));
        lobby = false;
        currentMap = votedMap();
        mapVotes.clear();
        if (currentMap == null) {
            ArenaUtils.broadcastToOps(level.getServer(), null, Component.translatable("arena.error.no_maps"));
            EntropyArena.LOGGER.error("No map found after voting!");
            disable();
            return;
        }
        currentGamemode = currentMap.getNewGamemode();
        Notification.toAll(Component.translatable("arena.message.game_start").withStyle(ChatFormatting.GREEN));
        Notification.toAll(Component.translatable("arena.message.map_info", currentMap.getName()).withStyle(ChatFormatting.YELLOW).append(getCurrentGamemode().getName()));
        if (isTimed()) {
            timer = ServerConfig.ROUND_SECONDS.get();
        }
        currentMap.backup(level);
        currentMap.load(level);
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(this));
        getCurrentGamemode().onMatchStart(this);
        level.players().forEach(this::onRespawn);
        RunningPacket.sendToEveryone(this);
        PacketDistributor.sendToAllPlayers(new ScoresPacket(currentGamemode.getScoreText(level)));
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Post(this));
    }

    private ArenaMap votedMap() {
        HashMap<String, Integer> voteMap = new HashMap<>();
        mapVotes.forEach((playerUUID, mapName) -> voteMap.put(mapName, voteMap.getOrDefault(mapName, 0) + 1));
        Optional<ArenaMap> selectedOptional = MapList.getMaps().stream().filter(map -> votableMaps.contains(map.getName())).max(Comparator.comparingInt(m -> voteMap.getOrDefault(m.getName(), 0)));
        return selectedOptional.orElse(null);
    }

    public void vote(ServerPlayer player, String mapName) {
        mapVotes.put(player.getUUID(), mapName);
        Notification.toPlayer(Component.translatable("arena.message.voted_for_map", mapName).withStyle(ChatFormatting.GREEN), player);
    }

    public void onLevelTick() {
        if (!running) {
            return;
        }
        if ((isTimed() || lobby) && level.getGameTime() % 20 == 0) {
            timer--;
            PacketDistributor.sendToAllPlayers(new TimerPacket(timer));
            if (timer <= 0) {
                if (lobby) {
                    EntropyArena.LOGGER.info("Starting game!");
                    onMatchStart();
                } else {
                    EntropyArena.LOGGER.info("Ending game!");
                    onMatchEnd();
                }
            }
        }
        if (inGame()) {
            getCurrentGamemode().onLevelTick(this);
            if (scoreShouldEndGame(getCurrentGamemode().getHighestScore())) {
                EntropyArena.LOGGER.info("Ending game due to winning score {}", getCurrentGamemode().getHighestScore());
                onMatchEnd();
            }
        }
    }

    public void onEntityTick(Entity entity) {
        if (running && entity instanceof ServerPlayer player && ServerConfig.GIVE_SATURATION.get()) {
            player.getFoodData().setFoodLevel(20);
        }
        if (inGame()) {
            if (entity instanceof ServerPlayer player && respawnTimes.containsKey(player.getUUID())) {
                if (respawnTimes.get(player.getUUID()) + ServerConfig.RESPAWN_DELAY.get() * 20L < level.getGameTime()) {
                    onRespawn(player);
                } else {
                    long secondsUntilRespawn = ServerConfig.RESPAWN_DELAY.get() - (level.getGameTime() - respawnTimes.get(player.getUUID())) / 20;
                    player.displayClientMessage(Component.translatable("arena.message.respawning", secondsUntilRespawn).withStyle(ChatFormatting.RED), true);
                }
            }
            getCurrentGamemode().onEntityTick(this, entity);
        }
    }

    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (inGame()) {
            if (ServerConfig.RESPAWN_DELAY.get() > 0) {
                if (!getCurrentGamemode().onDeath(this, player, source)) {
                    player.setGameMode(GameType.SPECTATOR);
                    respawnTimes.put(player.getUUID(), level.getGameTime());
                    Notification.toAll(player.getCombatTracker().getDeathMessage());
                }
                return true;
            }
            return getCurrentGamemode().onDeath(this, player, source);
        }
        return false;
    }

    public void onRespawn(ServerPlayer player) {
        respawnTimes.remove(player.getUUID());
        player.setGameMode(GameType.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        if (running && lobby && lobbyPos != null) {
            ArenaUtils.teleportToPos(player, lobbyPos);
        } else if (inGame()) {
            ArrayList<BlockPos> spawns = getCurrentGamemode().getValidSpawns(this, player);
            if (!spawns.isEmpty()) {
                ArenaUtils.teleportToPos(player, spawns.get(new Random().nextInt(spawns.size())));
            } else {
                player.setGameMode(GameType.SPECTATOR);
                if (currentMap != null)
                    player.teleportTo(currentMap.getCenter().x, currentMap.getCenter().y, currentMap.getCenter().z);
            }
            getCurrentGamemode().onRespawn(player);
            giveStarterGear(player);
        }
    }

    public void giveStarterGear(ServerPlayer player) {
        Loadout loadout = loadouts.get(loadoutSelections.getOrDefault(player.getUUID(), loadouts.keySet().stream().toList().getFirst()));
        loadout.giveToPlayer(player);
        getCurrentGamemode().onGiveLoadout(player, loadout);
        NeoForge.EVENT_BUS.post(new GiveLoadoutEvent(this, player, loadout));
    }

    public void addLoadout(ServerPlayer player, String name) {
        loadouts.put(name, new Loadout(player));
    }

    public void onJoin(ServerPlayer player) {
        if (inGame()) {
            PacketDistributor.sendToPlayer(player, GameInfoPacket.fromData(this));
            PacketDistributor.sendToPlayer(player, new ScoresPacket(getCurrentGamemode().getScoreText(level)));
            getCurrentGamemode().onJoin(this, player);
            onRespawn(player);
        }
        RunningPacket.sendToPlayer(this, player);
    }

    public void onLeave(ServerPlayer player) {
        if (inGame()) {
            getCurrentGamemode().onLeave(this, player);
        }
        sendToLobby(player);
    }

    public void onLevelClose() {
        if (currentMap != null) {
            currentMap.reset(level);
        }
    }

    public boolean inGame() {
        return running && !lobby && getCurrentMap() != null && getCurrentGamemode() != null;
    }

    public boolean isTimed() {
        return targetScore == 0;
    }

    public boolean scoreShouldEndGame(int score) {
        return !isTimed() && score >= targetScore;
    }

    public ArenaMap getCurrentMap() {
        return currentMap;
    }

    public ArenaGamemode getCurrentGamemode() {
        return currentGamemode;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isLobby() {
        return lobby;
    }

    public Set<String> getLoadouts() {
        return loadouts.keySet();
    }

    public void removeLoadout(String name) {
        loadouts.remove(name);
    }

    public void updateLoadout(ServerPlayer player, String name) {
        loadouts.put(name, new Loadout(player));
    }

    public void setLoadoutChoice(ServerPlayer player, String name) {
        if (loadouts.containsKey(name)) {
            loadoutSelections.put(player.getUUID(), name);
        }
    }
}
