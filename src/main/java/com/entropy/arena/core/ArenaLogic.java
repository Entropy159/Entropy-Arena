package com.entropy.arena.core;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.events.GiveLoadoutEvent;
import com.entropy.arena.api.events.MatchEndEvent;
import com.entropy.arena.api.events.MatchStartEvent;
import com.entropy.arena.api.events.TeleportToLobbyEvent;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.ArenaMapInfo;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.network.toClient.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaLogic {
    private static final HashMap<ResourceKey<Level>, ArenaLogic> INSTANCE_MAP = new HashMap<>();
    public static final int MAPS_FOR_VOTING = 3;

    private final ServerLevel level;
    private ArenaData data;

    public ArenaLogic(ServerLevel level) {
        this.level = level;
        this.data = ArenaData.get(level);
    }

    public static ArenaLogic get(ServerLevel level) {
        ArenaLogic logic = INSTANCE_MAP.computeIfAbsent(level.dimension(), (dim) -> new ArenaLogic(level));
        logic.data = ArenaData.get(level);
        return logic;
    }

    public @Nullable Component enable() {
        if (data.running) {
            return Component.translatable("arena.error.already_running");
        }
        if (data.lobbyPos == null) {
            return Component.translatable("arena.error.no_lobby");
        }
        if (data.mapList.mapListIsEmpty()) {
            return Component.translatable("arena.error.no_maps");
        }
        if (data.loadouts.isEmpty()) {
            return Component.translatable("arena.error.no_loadouts");
        }
        Notification.toAll(Component.translatable("arena.message.enable").withStyle(ChatFormatting.DARK_GREEN));
        data.timer = ServerConfig.INTERVAL_SECONDS.get();
        data.lobby = true;
        data.running = true;
        sendAllToLobby();
        return null;
    }

    public void disable() {
        if (data.running) {
            data.running = false;
            data.timer = 0;
            onMatchEnd();
        }
    }

    public void sendAllToLobby() {
        level.players().forEach(this::sendToLobby);
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(data));
    }

    private void sendToLobby(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Pre(player));
        if (data.lobbyPos == null) {
            return;
        }
        ArenaUtils.teleportToPos(player, data.lobbyPos);
        player.setGameMode(GameType.ADVENTURE);
        player.setGlowingTag(false);
        player.getInventory().clearContent();
        player.setHealth(player.getMaxHealth());
        level.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Post(player));
    }

    public void onMatchEnd() {
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Pre(level));
        Notification.toAll(Component.translatable("arena.message.game_over").withStyle(ChatFormatting.RED));
        PacketDistributor.sendToAllPlayers(new ScoresPacket(List.of()));
        sendAllToLobby();
        if (data.currentGamemode != null) {
            data.currentGamemode.onMatchEnd(level);
            data.currentGamemode = null;
        }
        if (data.currentMap != null) data.currentMap.reset(level, this::afterMapReset);
    }

    private void afterMapReset() {
        data.currentMap = null;
        if (data.running) {
            data.timer = ServerConfig.INTERVAL_SECONDS.get();
            PacketDistributor.sendToAllPlayers(new TimerPacket(data.timer));
            data.lobby = true;
        }
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(data));
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(data));
        ArenaUtils.playSoundForEveryone(level, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT);
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Post(level));
    }

    private void startMapVote() {
        data.mapVotes.clear();
        ArrayList<ArenaMap> maps = data.mapList.getMaps();
        ArrayList<ArenaMapInfo> mapInfos = new ArrayList<>();
        int mapCount = Math.min(MAPS_FOR_VOTING, maps.size());
        for (int i = 0; i < mapCount; i++) {
            int mapIndex = new Random().nextInt(maps.size());
            ArenaMap map = maps.get(mapIndex);
            maps.remove(map);
            data.votableMaps.add(map.getName());
            mapInfos.add(map.getInfo());
        }
        PacketDistributor.sendToAllPlayers(new VotableMapsPacket(mapInfos));
    }

    public void onMatchStart() {
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Pre(level));
        data.lobby = false;
        data.isTimed = votedForTimedMap();
        data.currentMap = votedMap();
        data.mapVotes.clear();
        if (data.currentMap == null) {
            ArenaUtils.broadcastToOps(level.getServer(), null, Component.translatable("arena.error.no_maps"));
            EntropyArena.LOGGER.error("No map found after voting!");
            disable();
            return;
        }
        data.currentGamemode = data.currentMap.getNewGamemode();
        Notification.toAll(Component.translatable("arena.message.game_start").withStyle(ChatFormatting.GREEN));
        data.currentMap.backup(level, data.currentGamemode.getPropertiesToLookFor(), this::afterMapLoad);
    }

    private void afterMapLoad() {
        data.currentMap.load(level);
        Notification.toAll(Component.translatable("arena.message.map_info", data.currentMap.getName()).withStyle(ChatFormatting.YELLOW).append(data.currentGamemode.getName()));
        if (data.isTimed) {
            data.timer = data.currentMap.getTimer();
        }
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(data));
        data.currentGamemode.onMatchStart(level);
        level.players().forEach(this::onRespawn);
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(data));
        PacketDistributor.sendToAllPlayers(new ScoresPacket(data.currentGamemode.getScoreText(level)));
        ArenaUtils.playSoundForEveryone(level, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.AMBIENT);
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Post(level));
    }

    private ArenaMap votedMap() {
        HashMap<String, Integer> voteMap = new HashMap<>();
        data.mapVotes.forEach((playerUUID, mapName) -> voteMap.put(mapName, voteMap.getOrDefault(mapName, 0) + 1));
        Optional<ArenaMap> selectedOptional = data.mapList.getMaps().stream().max(Comparator.comparingInt(m -> voteMap.getOrDefault(m.getName(), 0)));
        return selectedOptional.orElse(null);
    }

    private boolean votedForTimedMap() {
        long votesForTimed = data.typeVotes.values().stream().filter(b -> b).count();
        long votesForScore = data.typeVotes.values().stream().filter(b -> !b).count();
        return votesForTimed >= votesForScore;
    }

    public void voteForMap(ServerPlayer player, String mapName) {
        data.mapVotes.put(player.getUUID(), mapName);
        Notification.toPlayer(Component.translatable("arena.message.voted_for_map", mapName).withStyle(ChatFormatting.GREEN), player);
        ArenaUtils.playSoundForPlayer(level, player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.AMBIENT);
    }

    public void voteForType(ServerPlayer player, boolean isTimed) {
        data.typeVotes.put(player.getUUID(), isTimed);
        Notification.toPlayer(Component.translatable(isTimed ? "arena.message.voted_for_timed" : "arena.message.voted_for_score").withStyle(ChatFormatting.GREEN), player);
        ArenaUtils.playSoundForPlayer(level, player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.AMBIENT);
    }

    public void onLevelTick() {
        if (!data.running) {
            return;
        }
        if ((data.isTimed || data.lobby) && level.getGameTime() % 20 == 0 && !level.players().isEmpty()) {
            if (data.timer > 0) {
                data.timer--;
                PacketDistributor.sendToAllPlayers(new TimerPacket(data.timer));
                if (data.lobby && data.timer == ServerConfig.INTERVAL_SECONDS.get() - ServerConfig.RECAP_SECONDS.get()) {
                    startMapVote();
                }
                if (data.timer == 0) {
                    if (data.lobby) {
                        EntropyArena.LOGGER.info("Starting game!");
                        onMatchStart();
                    } else {
                        EntropyArena.LOGGER.info("Ending game!");
                        onMatchEnd();
                    }
                }
            }
        }
        if (data.inGame()) {
            data.currentGamemode.onLevelTick(level);
            if (!data.isTimed && data.currentMap.shouldScoreEndGame(data.currentGamemode.getHighestScore())) {
                EntropyArena.LOGGER.info("Ending game due to winning score {}", data.currentGamemode.getHighestScore());
                onMatchEnd();
            }
        }
        if (level.players().isEmpty()) {
            onMatchEnd();
        }
    }

    public void onEntityTick(Entity entity) {
        if (data.running && entity instanceof ServerPlayer player && ServerConfig.GIVE_SATURATION.get()) {
            player.getFoodData().setFoodLevel(20);
        }
        if (data.inGame()) {
            if (entity instanceof ServerPlayer player && data.respawnTimes.containsKey(player.getUUID())) {
                if (data.respawnTimes.get(player.getUUID()) + ServerConfig.RESPAWN_DELAY.get() * 20L < level.getGameTime()) {
                    onRespawn(player);
                } else {
                    long secondsUntilRespawn = ServerConfig.RESPAWN_DELAY.get() - (level.getGameTime() - data.respawnTimes.get(player.getUUID())) / 20;
                    player.displayClientMessage(Component.translatable("arena.message.respawning", secondsUntilRespawn).withStyle(ChatFormatting.RED), true);
                }
            }
            data.currentGamemode.onEntityTick(level, entity);
        }
    }

    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (data.inGame()) {
            if (ServerConfig.RESPAWN_DELAY.get() > 0) {
                if (!data.currentGamemode.onDeath(player, source)) {
                    player.setGameMode(GameType.SPECTATOR);
                    data.respawnTimes.put(player.getUUID(), level.getGameTime());
                    player.removeAllEffects();
                    Notification.toAll(player.getCombatTracker().getDeathMessage().copy().withStyle(ChatFormatting.RED));
                }
                return true;
            }
            return data.currentGamemode.onDeath(player, source);
        }
        return false;
    }

    public void onRespawn(ServerPlayer player) {
        data.respawnTimes.remove(player.getUUID());
        player.setGameMode(GameType.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        if (data.running && data.lobby && data.lobbyPos != null) {
            ArenaUtils.teleportToPos(player, data.lobbyPos);
        } else if (data.inGame()) {
            ArrayList<BlockPos> spawns = data.currentGamemode.getValidSpawns(player, data.currentMap);
            if (!spawns.isEmpty()) {
                ArenaUtils.teleportToPos(player, spawns.get(new Random().nextInt(spawns.size())));
            } else {
                player.setGameMode(GameType.SPECTATOR);
                if (data.currentMap != null)
                    player.teleportTo(data.currentMap.getCenter().x, data.currentMap.getCenter().y, data.currentMap.getCenter().z);
            }
            data.currentGamemode.onRespawn(player);
            giveStarterGear(player);
        }
    }

    public void giveStarterGear(ServerPlayer player) {
        Loadout loadout = data.loadouts.get(data.loadoutSelections.getOrDefault(player.getUUID(), data.loadouts.keySet().stream().collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
            Collections.shuffle(l);
            return l;
        })).getFirst()));
        loadout.giveToPlayer(player);
        data.currentGamemode.onGiveLoadout(player, loadout);
        NeoForge.EVENT_BUS.post(new GiveLoadoutEvent(player, loadout));
    }

    public void onJoin(ServerPlayer player) {
        if (data.inGame()) {
            PacketDistributor.sendToPlayer(player, GameInfoPacket.fromData(data));
            PacketDistributor.sendToPlayer(player, new ScoresPacket(data.currentGamemode.getScoreText(level)));
            data.currentGamemode.onJoin(player);
            onRespawn(player);
        }
        PacketDistributor.sendToPlayer(player, RunningPacket.fromData(data));
    }

    public void onLeave(ServerPlayer player) {
        if (data.inGame()) {
            data.currentGamemode.onLeave(player);
        }
        sendToLobby(player);
    }

    public void onLevelClose() {
        if (data.currentMap != null) {
            data.currentMap.reset(level, () -> {
            });
        }
        INSTANCE_MAP.remove(level.dimension());
    }
}
