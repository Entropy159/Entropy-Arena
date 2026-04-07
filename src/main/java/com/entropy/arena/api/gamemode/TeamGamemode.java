package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.network.toClient.ScoresPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TeamGamemode extends ArenaGamemode {
    private static final StreamCodec<ByteBuf, HashMap<ArenaTeam, Integer>> SCORE_MAP_CODEC = ByteBufCodecs.map(HashMap::new, ArenaTeam.STREAM_CODEC, ByteBufCodecs.INT);
    private static final StreamCodec<ByteBuf, HashMap<UUID, ArenaTeam>> TEAM_MAP_CODEC = ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ArenaTeam.STREAM_CODEC);

    private HashMap<ArenaTeam, Integer> scoreMap = new HashMap<>();
    private HashMap<UUID, ArenaTeam> teamMap = new HashMap<>();

    public TeamGamemode(ResourceLocation id, String name) {
        super(id, name);
    }

    @Override
    public int getHighestScore() {
        return scoreMap.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    @Override
    public void onMatchStart(ServerLevel level) {
        super.onMatchStart(level);
        ArenaData data = ArenaData.get(level);
        ArrayList<ArenaTeam> validTeams = data.currentMap.getTeams(level);
        validTeams.forEach(team -> setScore(team, 0));
        int index = 0;
        for (ServerPlayer player : level.players()) {
            ArenaTeam team = validTeams.get(index);
            team.setThisTeam(player);
            setPlayerTeam(player, team);
            index++;
            if (index >= validTeams.size()) {
                index = 0;
            }
        }
    }

    @Override
    public void onMatchEnd(ServerLevel level) {
        super.onMatchEnd(level);
        int winningScore = 0;
        boolean tied = true;
        ArenaTeam winningTeam = null;
        for (ArenaTeam team : scoreMap.keySet()) {
            int score = getScore(team);
            if (score == winningScore) {
                tied = true;
                winningTeam = null;
            } else if (score > winningScore) {
                tied = false;
                winningTeam = team;
                winningScore = score;
            }
        }
        if (winningScore == 0) {
            Notification.toAll(Component.translatable("arena.message.nobody_scored").withStyle(ChatFormatting.RED));
        } else if (tied) {
            Notification.toAll(Component.translatable("arena.message.game_tied").withStyle(ChatFormatting.YELLOW));
        } else {
            Notification.toAll(Component.translatable("arena.message.team_winner", winningTeam.getColoredName(), winningScore).withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public ArrayList<BlockPos> getValidSpawns(ServerPlayer player, ArenaMap map) {
        return map.getSpawns(player.serverLevel()).getOrDefault(teamMap.getOrDefault(player.getUUID(), ArenaTeam.NONE), new ArrayList<>());
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        if (arenaMap.getSpawns(level).keySet().stream().filter(team -> team != ArenaTeam.NONE).count() < 2)
            return Component.translatable("arena.error.not_enough_teams");
        return null;
    }

    @Override
    public void onJoin(ServerPlayer player) {
        super.onJoin(player);
        ArenaData data = ArenaData.get(player.serverLevel());
        List<ArenaTeam> validTeams = data.currentMap.getTeams(player.serverLevel()).stream().sorted(Comparator.comparingInt(t -> Math.toIntExact(teamMap.values().stream().filter(t2 -> t == t2).count()))).toList();
        if (!validTeams.isEmpty()) {
            ArenaTeam team = validTeams.getFirst();
            team.setThisTeam(player);
            setPlayerTeam(player, team);
        }
    }

    @Override
    public void onLeave(ServerPlayer player) {
        super.onLeave(player);
        setPlayerTeam(player, ArenaTeam.NONE);
    }

    @Override
    public void onGiveLoadout(ServerPlayer player, Loadout loadout) {
        super.onGiveLoadout(player, loadout);
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.is(ItemTags.DYEABLE)) {
                stack.set(DataComponents.DYED_COLOR, new DyedItemColor(getPlayerTeam(player).getColor(), true));
            }
        });
    }

    public void setScore(ArenaTeam team, int score) {
        scoreMap.put(team, score);
        PacketDistributor.sendToAllPlayers(new ScoresPacket(getScoreText(null)));
    }

    public int getScore(ArenaTeam team) {
        return scoreMap.getOrDefault(team, 0);
    }

    public void incrementScore(ArenaTeam team) {
        setScore(team, getScore(team) + 1);
    }

    public ArenaTeam getPlayerTeam(Player player) {
        return teamMap.getOrDefault(player.getUUID(), ArenaTeam.NONE);
    }

    public void setPlayerTeam(ServerPlayer player, ArenaTeam team) {
        if (team != ArenaTeam.NONE) {
            teamMap.put(player.getUUID(), team);
        } else {
            teamMap.remove(player.getUUID());
        }
    }

    public List<Component> getScoreText(ServerLevel level) {
        return scoreMap.entrySet().stream().sorted(Comparator.comparingInt(e -> -e.getValue())).map(e -> e.getKey().getScoreText(e.getValue())).toList();
    }

    @Override
    public ArenaTeam getTeamForBlock(ServerPlayer player) {
        return getPlayerTeam(player);
    }

    @Override
    public int modifyEntityColor(Entity entity, int color) {
        if (entity instanceof ServerPlayer player) {
            return teamMap.getOrDefault(player.getUUID(), ArenaTeam.NONE).getColor();
        }
        return super.modifyEntityColor(entity, color);
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        SCORE_MAP_CODEC.encode(buffer, scoreMap);
        TEAM_MAP_CODEC.encode(buffer, teamMap);
    }

    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        scoreMap = SCORE_MAP_CODEC.decode(buffer);
        teamMap = TEAM_MAP_CODEC.decode(buffer);
    }
}
