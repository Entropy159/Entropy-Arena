package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.events.KillStreakEvent;
import com.entropy.arena.api.events.LoadoutComponentEvent;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.loadout.LoadoutSerializer;
import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.util.ArenaTeam;
import com.entropy.arena.api.util.EventScheduler;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.CapturePointBlock;
import com.entropy.arena.core.blocks.PedestalBlock;
import com.entropy.arena.core.blocks.SpawnpointBlock;
import com.entropy.arena.core.blocks.TeamBlock;
import com.entropy.arena.core.items.DisguiseItem;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.entropy.arena.core.registry.ArenaStatTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class ArenaGamemode implements CustomPacketPayload, Supplier<ArenaGamemode> {
    private final ResourceLocation registryID;
    public HashMap<UUID, Integer> killStreak = new HashMap<>();

    public ArenaGamemode(ResourceLocation id) {
        registryID = id;
    }

    public ResourceLocation getRegistryID() {
        return registryID;
    }

    public Component getName() {
        return Component.translatable("arena.gamemode." + registryID.toLanguageKey());
    }

    public void onLevelTick(ServerLevel level) {

    }

    public void onEntityTick(ServerLevel level, Entity entity) {

    }

    /**
     * This method is called after a player is damaged.
     *
     * @param player The player being damaged
     * @param source The damage source
     */
    public void onPlayerHurt(ServerPlayer player, DamageSource source) {

    }

    /**
     * This method is called when the player dies. It drops items that either have
     * the SHOULD_DROP_ON_DEATH data component, or returns true from shouldDropOnDeath()
     *
     * @param player The player that died
     * @param source The damage source that caused the player to die
     */
    public void onDeath(ServerPlayer player, DamageSource source) {
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.getOrDefault(ArenaDataComponents.SHOULD_DROP_ON_DEATH, false) || shouldDropOnDeath().test(stack)) {
                player.drop(stack, true, false);
            }
        });
        LoadoutSerializerRegistry.clearAll(player);
        int oldValue = killStreak.getOrDefault(player.getUUID(), 0);
        killStreak.put(player.getUUID(), 0);
        EventScheduler.schedule(1, () -> NeoForge.EVENT_BUS.post(new KillStreakEvent(player, oldValue, 0)));
        if (source.getEntity() instanceof ServerPlayer killer) {
            int oldKillerValue = killStreak.getOrDefault(killer.getUUID(), 0);
            killStreak.put(killer.getUUID(), oldKillerValue + 1);
            EventScheduler.schedule(1, () -> NeoForge.EVENT_BUS.post(new KillStreakEvent(killer, oldKillerValue, killStreak.get(killer.getUUID()))));
        }
    }

    public Predicate<ItemStack> shouldDropOnDeath() {
        return stack -> false;
    }

    public void onRespawn(ServerPlayer player) {
    }

    public void onGiveLoadout(ServerPlayer player, Loadout loadout) {
        ArenaMap currentMap = ArenaData.get(player.serverLevel()).currentMap;
        boolean blocksAllowed = currentMap == null || currentMap.allowBlocks();
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TeamBlock) {
                serializer.setStack(player, slot, blocksAllowed ? TeamBlock.getStack(getTeamForBlock(player)) : ItemStack.EMPTY);
            }
            if (stack.has(ArenaDataComponents.ITEM_LIST)) {
                applyItemList(player, serializer, slot, stack, 0);
            }
        });
    }

    public void applyItemList(ServerPlayer player, LoadoutSerializer serializer, int slot, ItemStack stack, int recursionCount) {
        ArenaData data = ArenaData.get(player.serverLevel());
        ItemList list = data.itemLists.get(stack.get(ArenaDataComponents.ITEM_LIST));
        if (list != null) {
            ItemStack newStack = getItemFromList(player, list);
            newStack.setCount(stack.getCount());
            boolean shouldRecurse = newStack.has(ArenaDataComponents.ITEM_LIST) && recursionCount < 10;
            DataComponentPatch.Builder builder = DataComponentPatch.builder();
            stack.getComponents().forEach(typed -> {
                if (isComponentAllowed(typed, newStack)) {
                    builder.set(typed);
                }
            });
            newStack.applyComponents(builder.build());
            if (shouldRecurse) {
                applyItemList(player, serializer, slot, newStack, recursionCount + 1);
            } else {
                serializer.setStack(player, slot, newStack);
            }
        }
    }

    public boolean isComponentAllowed(TypedDataComponent<?> component, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new LoadoutComponentEvent(component, stack)).isAllowed();
    }

    public ItemStack getItemFromList(ServerPlayer player, ItemList list) {
        return list.get(0);
    }

    public boolean isValidLoadout(ServerPlayer player, Loadout loadout) {
        return !loadout.contains(player.serverLevel(), stack -> stack.getItem() instanceof DisguiseItem) && (loadout.getItemLists(player.serverLevel()).stream().anyMatch(this::isValidItemList) || loadout.getItemLists(player.serverLevel()).isEmpty());
    }

    public boolean isValidItemList(ItemList list) {
        return list.isRandom();
    }

    public boolean shouldWin(ServerLevel level, boolean timed, int timer, int targetScore) {
        return timed ? timer == 0 : getHighestScore() >= targetScore;
    }

    public void onJoin(ServerPlayer player) {
        sendToPlayer(player);
    }

    public void onLeave(ServerPlayer player) {

    }

    public void onMatchEnd(ServerLevel level) {
        level.players().forEach(player -> player.awardStat(ArenaStatTypes.MATCHES_FINISHED.get()));
        getWinners(level).forEach(player -> player.awardStat(ArenaStatTypes.MATCHES_WON.get()));
    }

    public abstract List<ServerPlayer> getWinners(ServerLevel level);

    public void onMatchStart(ServerLevel level) {
        level.players().forEach(player -> player.awardStat(ArenaStatTypes.MATCHES_PLAYED.get()));
    }

    public ArenaTeam getTeamForBlock(ServerPlayer player) {
        return ArenaTeam.NONE;
    }

    public abstract int getHighestScore();

    public abstract List<Component> getScoreText(ServerLevel level);

    public abstract ArrayList<BlockPos> getValidSpawns(ServerPlayer player, ArenaMap map);

    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        return arenaMap.getSpawns(level).isEmpty() ? Component.translatable("arena.error.no_spawns") : null;
    }

    public ArrayList<Property<?>> getPropertiesToLookFor() {
        ArrayList<Property<?>> list = new ArrayList<>();
        list.add(SpawnpointBlock.SPAWN_COLOR);
        list.add(CapturePointBlock.VISIBLE);
        list.add(PedestalBlock.GEM_COLOR);
        return list;
    }

    @OnlyIn(Dist.CLIENT)
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldShowNametag(Player other) {
        return true;
    }

    public void sendToAll() {
        PacketDistributor.sendToAllPlayers(this);
    }

    public void sendToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public void handleClientPacket(IPayloadContext ctx) {
        ClientData.currentGamemode = this;
    }

    public void encodeData(ByteBuf buffer) {

    }

    public void decodeData(ByteBuf buffer) {

    }

    public <T extends ArenaGamemode> StreamCodec<ByteBuf, T> getStreamCodec() {
        return StreamCodec.of((buffer, value) -> value.encodeData(buffer), buffer -> {
            @SuppressWarnings("unchecked") T value = (T) GamemodeRegistry.getNew(registryID);
            Objects.requireNonNull(value).decodeData(buffer);
            return value;
        });
    }

    @Override
    public @NotNull Type<ArenaGamemode> type() {
        return new Type<>(registryID);
    }

    public void registerPacket(PayloadRegistrar registrar) {
        registrar.playToClient(type(), getStreamCodec(), ArenaGamemode::handleClientPacket);
    }

    @Override
    public ArenaGamemode get() {
        try {
            return this.getClass().getDeclaredConstructor(ResourceLocation.class).newInstance(registryID);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            EntropyArena.LOGGER.error("Error creating new ArenaGamemode instance!", e);
            throw new RuntimeException(e);
        }
    }
}
