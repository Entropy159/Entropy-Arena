package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.PedestalBlock;
import com.entropy.arena.core.items.TeamGemItem;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.entropy.arena.core.registry.ArenaItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class CaptureTheFlag extends TeamGamemode {
    private static final StreamCodec<ByteBuf, HashMap<ArenaTeam, ArrayList<BlockPos>>> PEDESTAL_MAP_CODEC = ByteBufCodecs.map(HashMap::new, ArenaTeam.STREAM_CODEC, BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)));
    private static final StreamCodec<ByteBuf, HashMap<BlockPos, Boolean>> PEDESTAL_VALUE_MAP_CODEC = ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, ByteBufCodecs.BOOL);

    private HashMap<ArenaTeam, ArrayList<BlockPos>> pedestalPositions = new HashMap<>();
    private HashMap<BlockPos, Boolean> pedestalValueMap = new HashMap<>();

    public CaptureTheFlag() {
        super(EntropyArena.id("capture_the_flag"), "Capture the Flag");
    }

    @Override
    public void generateLang() {
        super.generateLang();
        EntropyArena.REGISTRATE.addRawLang("arena.message.ctf.flag_taken", "Team %s's flag has been taken by team %s");
        EntropyArena.REGISTRATE.addRawLang("arena.message.ctf.flag_returned", "Team %s's flag has been returned");
        EntropyArena.REGISTRATE.addRawLang("arena.message.ctf.flag_scored", "Team %s has scored");
        EntropyArena.REGISTRATE.addRawLang("arena.message.ctf.flag_dropped", "Team %s's flag has dropped out of the map");
        EntropyArena.REGISTRATE.addRawLang("arena.message.pedestal_invalid", "You cannot score on a pedestal that's been taken from");

        EntropyArena.REGISTRATE.addRawLang("arena.error.not_enough_pedestals", "Not enough pedestals");
    }

    @Override
    public void onMatchStart(ServerLevel level) {
        super.onMatchStart(level);
        pedestalPositions = ArenaData.get(level).currentMap.getBlockPropertyMap(level, PedestalBlock.GEM_COLOR);
        pedestalPositions.values().forEach(list -> list.forEach(pos -> pedestalValueMap.put(pos, true)));
        sendToAll();
    }

    @Override
    public void onEntityTick(ServerLevel level, Entity entity) {
        super.onEntityTick(level, entity);
        if (isTeamGem(entity)) {
            entity.setGlowingTag(true);
        }
        if (entity instanceof ServerPlayer player) {
            player.setGlowingTag(playerHasGem(player));
        }
    }

    private boolean isTeamGem(Entity entity) {
        return entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() instanceof TeamGemItem;
    }

    private boolean playerHasGem(ServerPlayer player) {
        return player.getInventory().hasAnyMatching(stack -> stack.getItem() instanceof TeamGemItem);
    }

    @Override
    public int modifyEntityColor(Entity entity, int color) {
        if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() instanceof TeamGemItem) {
            return itemEntity.getItem().getOrDefault(ArenaDataComponents.TEAM, ArenaTeam.NONE).getColor();
        }
        return super.modifyEntityColor(entity, color);
    }

    @Override
    public Predicate<ItemStack> shouldDropOnDeath() {
        return super.shouldDropOnDeath().or(stack -> stack.getItem() instanceof TeamGemItem);
    }

    private @Nullable BlockPos getPedestal(ArenaTeam team, int index) {
        ArrayList<BlockPos> pedestals = pedestalPositions.getOrDefault(team, new ArrayList<>());
        if (index >= pedestals.size()) {
            return null;
        }
        return pedestals.get(index);
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        if (pedestalPositions.size() > arenaMap.getSpawns(level).size())
            return Component.translatable("arena.error.not_enough_pedestals");
        return null;
    }

    private int getPedestalIndex(ArenaTeam team, BlockPos pos) {
        return pedestalPositions.getOrDefault(team, new ArrayList<>()).indexOf(pos);
    }

    public void takeGem(ServerPlayer player, BlockPos pos) {
        ArenaTeam gemTeam = player.serverLevel().getBlockState(pos).getValue(PedestalBlock.GEM_COLOR);
        ArenaTeam playerTeam = getPlayerTeam(player);
        if (gemTeam != playerTeam) {
            int flagIndex = getPedestalIndex(gemTeam, pos);
            if (flagIndex > -1) {
                PedestalBlock.setHasGem(player.serverLevel(), pos, false);
                pedestalValueMap.put(pos, false);
                ItemStack gem = new ItemStack(ArenaItems.TEAM_GEM.get());
                gem.set(ArenaDataComponents.PEDESTAL_INDEX, flagIndex);
                gem.set(ArenaDataComponents.TEAM, gemTeam);
                player.addItem(gem);
                Notification.toAll(Component.translatable("arena.message.ctf.flag_taken", gemTeam.getColoredName(), playerTeam.getColoredName()).withStyle(ChatFormatting.RED));
                player.serverLevel().playSound(null, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, SoundEvents.BEACON_DEACTIVATE, SoundSource.AMBIENT, 16, 1);
                sendToAll();
            }
        }
    }

    public void returnGem(ServerPlayer player, BlockPos pos, ItemStack gem) {
        ServerLevel level = player.serverLevel();
        ArenaTeam pedestalTeam = level.getBlockState(pos).getValue(PedestalBlock.GEM_COLOR);
        ArenaTeam gemTeam = ArenaTeam.getFromStack(gem);
        ArenaTeam playerTeam = getPlayerTeam(player);
        if (pedestalTeam == playerTeam && pedestalTeam == gemTeam) {
            int pedestalIndex = gem.getOrDefault(ArenaDataComponents.PEDESTAL_INDEX, -1);
            if (pedestalIndex > -1) {
                try {
                    BlockPos pedestalPosition = getPedestal(playerTeam, pedestalIndex);
                    if (pedestalPosition != null && !level.getBlockState(pedestalPosition).getValue(PedestalBlock.HAS_GEM)) {
                        PedestalBlock.setHasGem(level, pedestalPosition, true);
                        pedestalValueMap.put(pedestalPosition, true);
                        Notification.toAll(Component.translatable("arena.message.ctf.flag_returned", pedestalTeam.getColoredName()));
                        level.playSound(null, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 16, 1);
                        sendToAll();
                    }
                } catch (IndexOutOfBoundsException e) {
                    EntropyArena.LOGGER.error("Error returning gem", e);
                }
            }
        }
    }

    public void resetGem(ServerLevel level, ItemStack gem) {
        ArenaTeam flagTeam = ArenaTeam.getFromStack(gem);
        int flagIndex = gem.getOrDefault(ArenaDataComponents.PEDESTAL_INDEX, -1);
        try {
            BlockPos pos = getPedestal(flagTeam, flagIndex);
            if (flagTeam != null && pos != null && !level.getBlockState(pos).getValue(PedestalBlock.HAS_GEM)) {
                PedestalBlock.setHasGem(level, pos, true);
                pedestalValueMap.put(pos, true);
                Notification.toAll(Component.translatable("arena.message.ctf.flag_dropped", flagTeam.getColoredName()).withStyle(ChatFormatting.YELLOW));
                level.playSound(null, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 16, 1);
                sendToAll();
            }
        } catch (IndexOutOfBoundsException e) {
            EntropyArena.LOGGER.error("Error resetting gem", e);
        }
    }

    public void scoreGem(ServerPlayer player, BlockPos pos, ItemStack gem) {
        ServerLevel level = player.serverLevel();
        ArenaTeam pedestalTeam = level.getBlockState(pos).getValue(PedestalBlock.GEM_COLOR);
        ArenaTeam gemTeam = ArenaTeam.getFromStack(gem);
        ArenaTeam playerTeam = getPlayerTeam(player);
        if (pedestalTeam == playerTeam && playerTeam != gemTeam) {
            if (!level.getBlockState(pos).getValue(PedestalBlock.HAS_GEM)) {
                player.displayClientMessage(Component.translatable("arena.message.pedestal_invalid").withStyle(ChatFormatting.DARK_RED), true);
                return;
            }
            int pedestalIndex = gem.getOrDefault(ArenaDataComponents.PEDESTAL_INDEX, -1);
            if (pedestalIndex >= 0) {
                try {
                    BlockPos oldPedestal = getPedestal(gemTeam, pedestalIndex);
                    if (oldPedestal != null) {
                        PedestalBlock.setHasGem(level, oldPedestal, true);
                        pedestalValueMap.put(oldPedestal, true);
                        incrementScore(playerTeam);
                        gem.shrink(1);
                        Notification.toAll(Component.translatable("arena.message.ctf.flag_scored", pedestalTeam.getColoredName()).withStyle(ChatFormatting.GREEN));
                        level.playSound(null, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 16, 1);
                        sendToAll();
                    }
                } catch (IndexOutOfBoundsException e) {
                    EntropyArena.LOGGER.error("Error scoring with gem", e);
                }
            }
        }
    }

    @Override
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
        super.onClientRender(graphics, tracker);
        pedestalPositions.forEach((team, list) -> list.forEach(pos -> renderFlagIcon(team, pos, graphics, tracker)));
    }

    private void renderFlagIcon(ArenaTeam team, BlockPos pos, GuiGraphics graphics, DeltaTracker tracker) {
        int color = team.getColor();
        if (!pedestalValueMap.get(pos)) {
            color = ArenaUtils.lerpColors(team.getColor(), 0xFF000000, ArenaRenderingUtils.sineFromZeroToOne(6, tracker));
        }
        ArenaRenderingUtils.renderImageAtWorldPos(graphics, EntropyArena.id("flag"), pos.getCenter(), 16, color);
    }

    @Override
    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        PEDESTAL_MAP_CODEC.encode(buffer, pedestalPositions);
        PEDESTAL_VALUE_MAP_CODEC.encode(buffer, pedestalValueMap);
    }

    @Override
    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        pedestalPositions = PEDESTAL_MAP_CODEC.decode(buffer);
        pedestalValueMap = PEDESTAL_VALUE_MAP_CODEC.decode(buffer);
    }
}
