package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.api.client.ScreenAnchorPoint;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.CoOpGamemode;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.api.map.ArenaMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveSurvival extends CoOpGamemode {
    private static final StreamCodec<ByteBuf, List<BlockPos>> MOB_SPAWNS_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final int WAVE_INTERVAL_TICKS = 400;
    private static final List<EntityType<? extends Entity>> MOBS = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.BREEZE, EntityType.BLAZE, EntityType.PIGLIN_BRUTE, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.EVOKER, EntityType.VINDICATOR, EntityType.HUSK);
    private static final ArenaTeam ENEMY_TEAM = ArenaTeam.RED;

    private List<BlockPos> mobSpawns = new ArrayList<>();
    private int currentWave = 1;
    private int cooldownTicks = WAVE_INTERVAL_TICKS;
    private int mobCount = 0;
    private boolean isInterval = true;

    public WaveSurvival() {
        super(EntropyArena.id("wave_survival"), "Wave Survival");
    }

    @Override
    public void onMatchStart(ServerLevel level) {
        super.onMatchStart(level);
        mobSpawns = ArenaData.get(level).currentMap.getSpawns(level).get(ENEMY_TEAM);
    }

    @Override
    public void onLevelTick(ServerLevel level) {
        super.onLevelTick(level);
        if (isInterval) {
            tickCooldown();
            if (cooldownTicks == 0) {
                spawnMobs(level);
                isInterval = false;
            }
        } else {
            calculateMobCount(level, ArenaData.get(level).currentMap.getBoundingBox());
            if (mobCount == 0) {
                isInterval = true;
                cooldownTicks = WAVE_INTERVAL_TICKS;
                incrementScore();
                Notification.toAll(Component.translatable("arena.message.waves.survived_wave", currentWave).withStyle(ChatFormatting.GREEN));
                currentWave++;
            }
        }
        sendToAll();
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        if (!arenaMap.getSpawns(level).containsKey(ENEMY_TEAM))
            return Component.translatable("arena.error.no_enemy_spawns");
        return null;
    }

    public void tickCooldown() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    public void spawnMobs(ServerLevel level) {
        for (int mob = 0; mob < getMobCount(); mob++) {
            EntityType<? extends Entity> type = MOBS.get(new Random().nextInt(MOBS.size()));
            Entity newMob = type.spawn(level, getRandomMobSpawn(), MobSpawnType.MOB_SUMMONED);
            if (newMob != null) level.addFreshEntity(newMob);
        }
    }

    public BlockPos getRandomMobSpawn() {
        return mobSpawns.get(new Random().nextInt(mobSpawns.size()));
    }

    public int getMobCount() {
        return currentWave * 10;
    }

    public void calculateMobCount(ServerLevel level, AABB mapArea) {
        mobCount = level.getEntities(EntityTypeTest.forClass(Mob.class), mapArea, entity -> MOBS.contains(entity.getType())).size();
    }

    @Override
    public void generateLang() {
        super.generateLang();
        EntropyArena.REGISTRATE.addRawLang("arena.message.waves.interval", "Interval: %s");
        EntropyArena.REGISTRATE.addRawLang("arena.message.waves.mob_count", "Mobs: %s/%s");
        EntropyArena.REGISTRATE.addRawLang("arena.message.waves.survived_wave", "Survived wave %s");
    }

    @Override
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
        super.onClientRender(graphics, tracker);
        if (isInterval) {
            ArenaRenderingUtils.renderText(graphics, Component.translatable("arena.message.waves.interval", cooldownTicks / 20).withStyle(ChatFormatting.YELLOW), ScreenAnchorPoint.TOP_CENTER);
        } else {
            ArenaRenderingUtils.renderText(graphics, Component.translatable("arena.message.waves.mob_count", mobCount, getMobCount()).withStyle(ChatFormatting.YELLOW), ScreenAnchorPoint.TOP_CENTER);
        }
    }

    @Override
    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        MOB_SPAWNS_CODEC.encode(buffer, mobSpawns);
        ByteBufCodecs.INT.encode(buffer, currentWave);
        ByteBufCodecs.INT.encode(buffer, cooldownTicks);
        ByteBufCodecs.INT.encode(buffer, mobCount);
        ByteBufCodecs.BOOL.encode(buffer, isInterval);
    }

    @Override
    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        mobSpawns = MOB_SPAWNS_CODEC.decode(buffer);
        currentWave = ByteBufCodecs.INT.decode(buffer);
        cooldownTicks = ByteBufCodecs.INT.decode(buffer);
        mobCount = ByteBufCodecs.INT.decode(buffer);
        isInterval = ByteBufCodecs.BOOL.decode(buffer);
    }
}
