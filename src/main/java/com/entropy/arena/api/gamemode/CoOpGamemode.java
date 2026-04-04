package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.map.ArenaMap;
import com.entropy.arena.core.network.toClient.ScoresPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CoOpGamemode extends ArenaGamemode {
    private int collectiveScore = 0;

    public CoOpGamemode(ResourceLocation id, String name) {
        super(id, name);
    }

    @Override
    public int getHighestScore() {
        return collectiveScore;
    }

    @Override
    public void onMatchEnd(ServerLevel level) {
        super.onMatchEnd(level);
        Notification.toAll(Component.translatable("arena.message.collective_winner", collectiveScore).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public ArrayList<BlockPos> getValidSpawns(ServerPlayer player, ArenaMap map) {
        return map.getSpawns(player.serverLevel()).get(ArenaTeam.NONE);
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        if (!arenaMap.getSpawns(level).containsKey(ArenaTeam.NONE)) return Component.translatable("arena.error.no_spawns");
        return null;
    }

    public void setScore(int value) {
        collectiveScore = value;
        PacketDistributor.sendToAllPlayers(new ScoresPacket(getScoreText(null)));
    }

    public int getScore() {
        return collectiveScore;
    }

    public void incrementScore() {
        setScore(getScore() + 1);
    }

    @Override
    public List<Component> getScoreText(ServerLevel level) {
        return List.of(Component.translatable("arena.hud.score_value", getScore()).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public int modifyEntityColor(Entity entity, int color) {
        if (entity instanceof Player) {
            return 0xFF00FF00;
        }
        return super.modifyEntityColor(entity, color);
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        ByteBufCodecs.INT.encode(buffer, collectiveScore);
    }

    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        collectiveScore = ByteBufCodecs.INT.decode(buffer);
    }
}
