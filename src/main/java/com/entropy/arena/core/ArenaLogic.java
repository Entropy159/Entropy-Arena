package com.entropy.arena.core;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.events.GiveLoadoutEvent;
import com.entropy.arena.api.events.MatchEndEvent;
import com.entropy.arena.api.events.MatchStartEvent;
import com.entropy.arena.api.events.TeleportToLobbyEvent;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.ArenaMapBackup;
import com.entropy.arena.api.util.ArenaGameType;
import com.entropy.arena.api.util.ArenaUtils;
import com.entropy.arena.api.util.Notification;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.network.toClient.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaLogic {
    private static final HashMap<MinecraftServer, ArenaLogic> INSTANCE_MAP = new HashMap<>();
    public static final int MAPS_FOR_VOTING = 3;

    private final MinecraftServer server;
    private final ArenaData data;
    private @NotNull ServerLevel currentLevel;

    public ArenaLogic(MinecraftServer server) {
        this.server = server;
        this.currentLevel = server.overworld();
        this.data = ArenaData.get(server);
    }

    public static ArenaLogic get(MinecraftServer server) {
        return INSTANCE_MAP.computeIfAbsent(server, ArenaLogic::new);
    }

    public static ArenaLogic get(ServerLevel level) {
        return get(level.getServer());
    }

    public static @Nullable ArenaLogic get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : get(server);
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
        if (data.backupState == ArenaMapBackup.BackupState.RESTORING) {
            return Component.translatable("arena.error.restoring_backup");
        }
        if (data.backupState == ArenaMapBackup.BackupState.BACKING_UP) {
            return Component.translatable("arena.error.backing_up");
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
            endMatch();
        }
    }

    public void sendAllToLobby() {
        server.getPlayerList().getPlayers().forEach(this::sendToLobby);
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(server));
    }

    private void sendToLobby(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Pre(player));
        if (data.lobbyPos == null) {
            return;
        }
        ArenaUtils.instantTeleport(player, data.lobbyPos);
        player.setGameMode(GameType.ADVENTURE);
        player.setGlowingTag(false);
        player.setRespawnPosition(data.lobbyPos.dimension(), data.lobbyPos.pos(), 0, true, false);
        LoadoutSerializerRegistry.clearAll(player);
        player.setHealth(player.getMaxHealth());
        server.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
        NeoForge.EVENT_BUS.post(new TeleportToLobbyEvent.Post(player));
    }

    public void endMatch() {
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Pre(currentLevel));
        Notification.toAll(Component.translatable("arena.message.game_over").withStyle(ChatFormatting.RED));
        data.loadoutSelections.clear();
        data.votableMaps.clear();
        data.mapVotes.clear();
        PacketDistributor.sendToAllPlayers(new ScoresPacket(List.of()));
        sendAllToLobby();
        if (data.currentGamemode != null) {
            data.currentGamemode.onMatchEnd(currentLevel);
            data.currentGamemode = null;
        }
        PacketDistributor.sendToAllPlayers(new ConfigOverridesPacket(new HashMap<>()));
        if (data.currentMap != null) {
            data.currentMap.reset(currentLevel, this::afterMapReset);
        } else {
            afterMapReset();
        }
    }

    private void afterMapReset() {
        data.currentMap = null;
        if (data.running) {
            data.timer = ServerConfig.INTERVAL_SECONDS.get();
            PacketDistributor.sendToAllPlayers(new TimerPacket(data.timer));
            data.lobby = true;
        }
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(server));
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(data));
        ArenaUtils.playSoundForEveryone(server, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT);
        NeoForge.EVENT_BUS.post(new MatchEndEvent.Post(currentLevel));
        if (data.lobbyPos != null) {
            ServerLevel lobbyLevel = server.getLevel(data.lobbyPos.dimension());
            currentLevel = lobbyLevel == null ? server.overworld() : lobbyLevel;
        }
    }

    private void startMapVote() {
        data.mapVotes.clear();
        ArrayList<ArenaMap> maps = data.mapList.getValidMaps();
        int mapCount = Math.min(MAPS_FOR_VOTING, maps.size());
        for (int i = 0; i < mapCount; i++) {
            int mapIndex = new Random().nextInt(maps.size());
            ArenaMap map = maps.get(mapIndex);
            maps.remove(map);
            data.votableMaps.add(map.getName());
        }
        sendMapVotes(true);
    }

    public void startMatch() {
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Pre(server));
        data.gameType = getVotedGameType();
        data.currentMap = getVotedMap();
        data.mapVotes.clear();
        data.typeVotes.clear();
        data.votableMaps.clear();
        if (data.currentMap == null) {
            ArenaUtils.broadcastToOps(server, null, Component.translatable("arena.error.no_maps"));
            EntropyArena.LOGGER.error("No map found after voting!");
            disable();
            return;
        }
        data.currentGamemode = data.currentMap.getNewGamemode();
        if (data.currentGamemode == null) {
            Notification.toAll(Component.translatable("arena.error.no_gamemode", data.currentMap.getGamemodeID().toString()).withStyle(ChatFormatting.RED));
            disable();
            return;
        }
        ServerLevel mapLevel = data.currentMap.getLevel(server);
        if (mapLevel == null) {
            ArenaUtils.broadcastToOps(server, null, Component.translatable("arena.error.no_level", data.currentMap.getDimension().location()));
            disable();
            return;
        }
        currentLevel = mapLevel;
        Notification.toAll(Component.translatable("arena.message.game_start").withStyle(ChatFormatting.GREEN));
        data.backup(this::afterMapLoad);
    }

    private void afterMapLoad() {
        data.lobby = false;
        data.currentMap.load(currentLevel);
        data.currentMap.syncConfig(currentLevel);
        if (ArenaUtils.getPerMapConfig(ServerConfig.SET_WORLD_BORDER, ModConfig.Type.SERVER, EntropyArena.MODID)) {
            data.currentMap.setWorldBorder(currentLevel);
        }
        Notification.toAll(Component.translatable("arena.message.map_info", data.currentMap.getName()).withStyle(ChatFormatting.YELLOW).append(data.currentGamemode.getName()));
        if (data.gameType == ArenaGameType.TIMED) {
            data.timer = ArenaUtils.getPerMapConfig(ServerConfig.ROUND_SECONDS, ModConfig.Type.SERVER, EntropyArena.MODID);
        }
        PacketDistributor.sendToAllPlayers(GameInfoPacket.fromData(data));
        data.currentGamemode.onMatchStart(currentLevel);
        server.getPlayerList().getPlayers().forEach(player -> {
            sendValidLoadouts(player);
            respawn(player);
        });
        PacketDistributor.sendToAllPlayers(RunningPacket.fromData(server));
        PacketDistributor.sendToAllPlayers(new ScoresPacket(data.currentGamemode.getScoreText(currentLevel)));
        ArenaUtils.playSoundForEveryone(server, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.AMBIENT);
        NeoForge.EVENT_BUS.post(new MatchStartEvent.Post(currentLevel));
    }

    public void sendValidLoadouts(ServerPlayer player) {
        Map<String, Loadout> validLoadouts = getValidLoadouts(player);
        if (validLoadouts.size() > 1) {
            PacketDistributor.sendToPlayer(player, new LoadoutsPacket(validLoadouts.keySet().stream().toList()));
        }
    }

    private ArenaMap getVotedMap() {
        HashMap<String, Integer> voteMap = new HashMap<>();
        data.mapVotes.forEach((playerUUID, mapName) -> voteMap.put(mapName, voteMap.getOrDefault(mapName, 0) + 1));
        Optional<ArenaMap> selectedOptional = data.mapList.getValidMaps().stream().max(Comparator.comparingInt(m -> voteMap.getOrDefault(m.getName(), 0)));
        return selectedOptional.orElse(null);
    }

    private ArenaGameType getVotedGameType() {
        if (data.typeVotes.isEmpty()) {
            return ArenaGameType.TIMED;
        }
        return data.typeVotes.values().stream().sorted(Comparator.comparingLong(type -> data.typeVotes.values().stream().filter(type::equals).count())).toList().getLast();
    }

    public void voteForMap(ServerPlayer player, String mapName) {
        data.mapVotes.put(player.getUUID(), mapName);
        Notification.toPlayer(Component.translatable("arena.message.voted_for_map", mapName).withStyle(ChatFormatting.GREEN), player);
        ArenaUtils.playSoundForPlayer(player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.AMBIENT);
        sendMapVotes(false);
    }

    public void voteForType(ServerPlayer player, ArenaGameType type) {
        data.typeVotes.put(player.getUUID(), type);
        Notification.toPlayer(type.getVotedComponent().withStyle(ChatFormatting.GREEN), player);
        ArenaUtils.playSoundForPlayer(player, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.AMBIENT);
        sendMapVotes(false);
    }

    private void sendMapVotes(boolean force) {
        PacketDistributor.sendToAllPlayers(VotableMapsPacket.fromData(data, force));
    }

    public void selectLoadout(ServerPlayer player, String loadout) {
        if (data.setLoadoutChoice(player, loadout) == null) {
            giveStarterGear(player);
        }
        Notification.toPlayer(Component.translatable("arena.message.selected_loadout", loadout).withStyle(ChatFormatting.GREEN), player);
    }

    public void onServerTick() {
        if (!data.running) {
            return;
        }
        if ((data.gameType == ArenaGameType.TIMED || data.lobby) && currentLevel.getGameTime() % 20 == 0 && !server.getPlayerList().getPlayers().isEmpty()) {
            if (data.timer > 0) {
                data.timer--;
                PacketDistributor.sendToAllPlayers(new TimerPacket(data.timer));
                if (data.lobby && data.timer == ServerConfig.INTERVAL_SECONDS.get() - ServerConfig.RECAP_SECONDS.get()) {
                    startMapVote();
                }
                if (data.timer == 0) {
                    if (data.lobby) {
                        EntropyArena.LOGGER.info("Starting game!");
                        startMatch();
                    }
                }
            }
        }
        if (data.inGame()) {
            data.currentGamemode.onLevelTick(currentLevel);
            if (data.currentGamemode.shouldWin(currentLevel, data.gameType.isTimed(), data.timer, ArenaUtils.getPerMapConfig(ServerConfig.TARGET_SCORE, ModConfig.Type.SERVER, EntropyArena.MODID))) {
                EntropyArena.LOGGER.info("Ending game!");
                endMatch();
            }
        }
        if (server.getPlayerList().getPlayers().isEmpty()) {
            endMatch();
        }
    }

    public void onEntityTick(Entity entity) {
        if (data.running && entity instanceof ServerPlayer player && ArenaUtils.getPerMapConfig(ServerConfig.GIVE_SATURATION, ModConfig.Type.SERVER, EntropyArena.MODID)) {
            player.getFoodData().setFoodLevel(20);
        }
        if (data.inGame()) {
            if (entity instanceof ServerPlayer player && data.respawnTimes.containsKey(player.getUUID())) {
                if (data.respawnTimes.get(player.getUUID()) + ArenaUtils.getPerMapConfig(ServerConfig.RESPAWN_DELAY, ModConfig.Type.SERVER, EntropyArena.MODID) * 20L < currentLevel.getGameTime()) {
                    respawn(player);
                } else {
                    long secondsUntilRespawn = ArenaUtils.getPerMapConfig(ServerConfig.RESPAWN_DELAY, ModConfig.Type.SERVER, EntropyArena.MODID) - (currentLevel.getGameTime() - data.respawnTimes.get(player.getUUID())) / 20;
                    player.displayClientMessage(Component.translatable("arena.message.respawning", secondsUntilRespawn).withStyle(ChatFormatting.RED), true);
                }
            }
            data.currentGamemode.onEntityTick(currentLevel, entity);
        }
    }

    public void onDeath(ServerPlayer player, DamageSource source) {
        if (data.inGame()) {
            player.setRespawnPosition(currentLevel.dimension(), player.blockPosition(), player.getYRot(), true, false);
            data.currentGamemode.onDeath(player, source);
        }
    }

    public void onRespawn(ServerPlayer player) {
        if (data.inGame()) {
            if (ArenaUtils.getPerMapConfig(ServerConfig.RESPAWN_DELAY, ModConfig.Type.SERVER, EntropyArena.MODID) > 0) {
                data.respawnTimes.put(player.getUUID(), currentLevel.getGameTime());
                player.setGameMode(GameType.SPECTATOR);
            } else {
                respawn(player);
            }
        }
    }

    public void respawn(ServerPlayer player) {
        data.respawnTimes.remove(player.getUUID());
        player.setGameMode(GameType.ADVENTURE);
        AttributeInstance attribute = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(ArenaUtils.getPerMapConfig(ServerConfig.MAX_HEALTH, ModConfig.Type.SERVER, EntropyArena.MODID));
        }
        player.setHealth(player.getMaxHealth());
        if (data.running && data.lobby && data.lobbyPos != null) {
            ArenaUtils.instantTeleport(player, data.lobbyPos);
        } else if (data.inGame()) {
            data.spawnProtection.put(player.getUUID(), currentLevel.getGameTime());
            PacketDistributor.sendToPlayer(player, new RespawnPacket(currentLevel.getGameTime()));
            ArrayList<BlockPos> spawns = data.currentGamemode.getValidSpawns(player, data.currentMap);
            if (!spawns.isEmpty()) {
                ArenaUtils.instantTeleport(player, GlobalPos.of(currentLevel.dimension(), spawns.get(new Random().nextInt(spawns.size()))));
            } else {
                player.setGameMode(GameType.SPECTATOR);
                if (data.currentMap != null) {
                    ArenaUtils.instantTeleport(player, GlobalPos.of(currentLevel.dimension(), BlockPos.containing(data.currentMap.getCenter())));
                }
            }
            data.currentGamemode.onRespawn(player);
            if (data.loadoutSelections.containsKey(player.getUUID()) || getValidLoadouts(player).size() < 2) {
                giveStarterGear(player);
            }
        }
    }

    public void giveStarterGear(ServerPlayer player) {
        if (data.running && !data.lobby) {
            Map<String, Loadout> validLoadouts = getValidLoadouts(player);
            Loadout loadout = validLoadouts.get(data.loadoutSelections.getOrDefault(player.getUUID(), validLoadouts.keySet().stream().collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                Collections.shuffle(l);
                return l;
            })).getFirst()));
            loadout.giveToPlayer(player);
            data.currentGamemode.onGiveLoadout(player, loadout);
            NeoForge.EVENT_BUS.post(new GiveLoadoutEvent(player, loadout));
        }
    }

    public Map<String, Loadout> getValidLoadouts(ServerPlayer player) {
        return data.loadouts.entrySet().stream().filter(entry -> entry.getValue().isEnabled() && data.currentGamemode.isValidLoadout(player, entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void onJoin(ServerPlayer player) {
        if (data.inGame()) {
            PacketDistributor.sendToPlayer(player, GameInfoPacket.fromData(data));
            PacketDistributor.sendToPlayer(player, new ScoresPacket(data.currentGamemode.getScoreText(currentLevel)));
            sendValidLoadouts(player);
            data.currentGamemode.onJoin(player);
            respawn(player);
        }
        PacketDistributor.sendToPlayer(player, RunningPacket.fromData(server));
    }

    public void onLeave(ServerPlayer player) {
        if (data.inGame()) {
            data.currentGamemode.onLeave(player);
        }
        sendToLobby(player);
    }

    public void onServerClose() {
        INSTANCE_MAP.remove(server);
    }

    public boolean isSpawnProtected(ServerPlayer player) {
        return data.running && (data.lobby || data.spawnProtection.getOrDefault(player.getUUID(), 0L) + ArenaUtils.getPerMapConfig(ServerConfig.SPAWN_PROTECTION, ModConfig.Type.SERVER, EntropyArena.MODID) * 20L >= currentLevel.getGameTime());
    }
}
