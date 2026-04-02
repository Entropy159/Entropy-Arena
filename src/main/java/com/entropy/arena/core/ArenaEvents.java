package com.entropy.arena.core;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.blocks.TeamBlock;
import com.entropy.arena.api.events.ShouldBlockBeInfiniteEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class ArenaEvents {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ArenaData data = ArenaData.get(level);
            data.onLevelTick();
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            ArenaData data = ArenaData.get(level);
            data.onEntityTick(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(level);
            if (data.onDeath(player, event.getSource())) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(level);
            data.onRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(level);
            level.getScoreboard().removePlayerFromTeam(player.getScoreboardName());
            data.onJoin(player);
        }
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(level);
            data.onLeave(player);
        }
    }

    @SubscribeEvent
    public static void onLevelClose(ServerStoppingEvent event) {
        event.getServer().getAllLevels().forEach(level -> ArenaData.get(level).onLevelClose());
    }

    @SubscribeEvent
    public static void infiniteTeamBlocks(ShouldBlockBeInfiniteEvent event) {
        if (event.getBlock() instanceof TeamBlock) {
            event.setInfinite(true);
        }
    }
}
