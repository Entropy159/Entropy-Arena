package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.Notification;
import com.entropy.arena.core.map.ArenaMap;
import com.entropy.arena.core.network.toClient.ScoresPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class FFAGamemode extends ArenaGamemode {
    private static final StreamCodec<ByteBuf, HashMap<UUID, Integer>> SCORE_MAP_CODEC = ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.INT);
    public HashMap<UUID, Integer> scoreMap = new HashMap<>();

    public FFAGamemode(ResourceLocation id, String name) {
        super(id, name);
    }

    @Override
    public int getHighestScore() {
        return scoreMap.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    @Override
    public void onMatchStart(ServerLevel level) {
        super.onMatchStart(level);
        level.players().forEach(player -> scoreMap.put(player.getUUID(), 0));
    }

    @Override
    public void onMatchEnd(ServerLevel level) {
        super.onMatchEnd(level);
        int winningScore = 0;
        boolean tied = true;
        ServerPlayer winningPlayer = null;
        for (ServerPlayer player : level.players()) {
            int score = scoreMap.getOrDefault(player.getUUID(), 0);
            if (score > winningScore) {
                tied = false;
                winningPlayer = player;
                winningScore = score;
            } else if (score == winningScore) {
                tied = true;
                winningPlayer = null;
            }
        }
        if (winningScore == 0) {
            Notification.toAll(Component.translatable("arena.message.nobody_scored").withStyle(ChatFormatting.RED));
        } else if (tied) {
            Notification.toAll(Component.translatable("arena.message.game_tied").withStyle(ChatFormatting.YELLOW));
        } else {
            Notification.toAll(Component.translatable("arena.message.player_winner", winningPlayer.getDisplayName(), winningScore).withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public ArrayList<BlockPos> getValidSpawns(ServerPlayer player, ArenaMap map) {
        return map.getSpawns(player.serverLevel()).getOrDefault(ArenaTeam.NONE, new ArrayList<>());
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component errorMessage = super.validateMap(level, arenaMap);
        if (errorMessage != null) return errorMessage;
        return arenaMap.getSpawns(level).containsKey(ArenaTeam.NONE) ? null : Component.translatable("arena.error.no_spawns");
    }

    @Override
    public void onJoin(ServerPlayer player) {
        super.onJoin(player);
        scoreMap.put(player.getUUID(), 0);
    }

    @Override
    public void onLeave(ServerPlayer player) {
        super.onLeave(player);
        scoreMap.remove(player.getUUID());
    }

    public void setScore(ServerPlayer player, int score) {
        scoreMap.put(player.getUUID(), score);
        PacketDistributor.sendToAllPlayers(new ScoresPacket(getScoreText(player.serverLevel())));
    }

    public int getScore(ServerPlayer player) {
        return scoreMap.getOrDefault(player.getUUID(), 0);
    }

    public void incrementScore(ServerPlayer player) {
        setScore(player, getScore(player) + 1);
    }

    public List<Component> getScoreText(ServerLevel level) {
        return scoreMap.entrySet().stream().filter(e -> level.getPlayerByUUID(e.getKey()) != null).sorted(Comparator.comparingInt(e -> -e.getValue())).map(e -> {
            Player player = level.getPlayerByUUID(e.getKey());
            if (player != null) {
                return player.getDisplayName().copy().append(": " + e.getValue());
            }
            return Component.nullToEmpty(null);
        }).toList();
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        SCORE_MAP_CODEC.encode(buffer, scoreMap);
    }

    public void decodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        scoreMap = SCORE_MAP_CODEC.decode(buffer);
    }
}
