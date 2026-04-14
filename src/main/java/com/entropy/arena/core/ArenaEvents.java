package com.entropy.arena.core;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.events.IgnoreAdventureModeEvent;
import com.entropy.arena.api.events.LoadoutComponentEvent;
import com.entropy.arena.api.events.ShouldBlockBeInfiniteEvent;
import com.entropy.arena.core.blocks.TeamBlock;
import com.entropy.arena.core.gamemodes.CaptureTheFlag;
import com.entropy.arena.core.registry.ArenaTags;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

@EventBusSubscriber
public class ArenaEvents {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ArenaLogic.get(level).onLevelTick();
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            ArenaLogic.get(level).onEntityTick(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            if (ArenaLogic.get(level).onDeath(player, event.getSource())) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaLogic.get(level).onRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaLogic.get(level).onJoin(player);
        }
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getEntity() instanceof ServerPlayer player) {
            ArenaLogic.get(level).onLeave(player);
        }
    }

    @SubscribeEvent
    public static void onLevelClose(ServerStoppingEvent event) {
        event.getServer().getAllLevels().forEach(level -> ArenaLogic.get(level).onLevelClose());
    }

    @SubscribeEvent
    public static void infiniteTeamBlocks(ShouldBlockBeInfiniteEvent event) {
        if (event.getBlock() instanceof TeamBlock) {
            event.setInfinite(true);
        }
    }

    @SubscribeEvent
    public static void adventureModeBypass(IgnoreAdventureModeEvent event) {
        if (event.isPlacing() && event.getHeldItem().getItem() instanceof BlockItem bi && bi.getBlock() instanceof TeamBlock) {
            event.setBypass(!event.getState().is(ArenaTags.TEAM_BLOCK_INVALID));
        }
        if (!event.isPlacing() && event.getState().getBlock() instanceof TeamBlock) {
            event.setBypass(true);
        }
    }

    @SubscribeEvent
    public static void allowComponents(LoadoutComponentEvent event) {
        List<DataComponentType<?>> allowedComponents = List.of(DataComponents.UNBREAKABLE, DataComponents.ENCHANTMENTS);

        if (allowedComponents.contains(event.getComponent().type())) {
            event.setAllowed(true);
        }
    }

    @SubscribeEvent
    public static void itemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player && ArenaData.get(player.serverLevel()).currentGamemode instanceof CaptureTheFlag ctf) {
            if (ctf.isTeamGem(event.getItemEntity()) && ctf.playerHasGem(player)) {
                event.setCanPickup(TriState.FALSE);
            }
        }
    }
}
