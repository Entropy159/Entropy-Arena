package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.events.LoadoutComponentEvent;
import com.entropy.arena.api.loadout.ItemList;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.api.loadout.LoadoutSerializer;
import com.entropy.arena.api.loadout.LoadoutSerializerRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.CapturePointBlock;
import com.entropy.arena.core.blocks.PedestalBlock;
import com.entropy.arena.core.blocks.SpawnpointBlock;
import com.entropy.arena.core.blocks.TeamBlock;
import com.entropy.arena.core.registry.ArenaDataComponents;
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
import net.minecraft.world.entity.player.Inventory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class ArenaGamemode implements CustomPacketPayload {
    private final ResourceLocation id;
    private final String name;

    public ArenaGamemode(ResourceLocation id, String name) {
        this.id = id;
        this.name = name;
    }

    public Component getName() {
        return Component.translatable("arena.gamemode." + getRegistryID().toLanguageKey());
    }

    public ResourceLocation getRegistryID() {
        return id;
    }

    public void generateLang() {
        EntropyArena.REGISTRATE.addRawLang("arena.gamemode." + getRegistryID().toLanguageKey(), name);
    }

    public void onLevelTick(ServerLevel level) {

    }

    public void onEntityTick(ServerLevel level, Entity entity) {

    }

    /**
     * This method is called when the player dies. It drops items that either have
     * the SHOULD_DROP_ON_DEATH data component, or returns true from shouldDropOnDeath()
     *
     * @param player The player that died
     * @param source The damage source that caused the player to die
     * @return Whether to prevent the player from dying or not
     */
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getOrDefault(ArenaDataComponents.SHOULD_DROP_ON_DEATH, false) || shouldDropOnDeath().test(stack)) {
                player.drop(stack, true, false);
            }
        }
        player.getInventory().clearContent();
        return false;
    }

    public Predicate<ItemStack> shouldDropOnDeath() {
        return stack -> false;
    }

    public void onRespawn(ServerPlayer player) {
    }

    public void onGiveLoadout(ServerPlayer player, Loadout loadout) {
        LoadoutSerializerRegistry.forEachStack(player, (serializer, slot, stack) -> {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TeamBlock) {
                serializer.setStack(player, slot, TeamBlock.getStack(getTeamForBlock(player)));
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
        return !NeoForge.EVENT_BUS.post(new LoadoutComponentEvent(component, stack)).isCanceled();
    }

    public ItemStack getItemFromList(ServerPlayer player, ItemList list) {
        return list.get(player.serverLevel().registryAccess(), 0);
    }

    public void onJoin(ServerPlayer player) {
        sendToPlayer(player);
    }

    public void onLeave(ServerPlayer player) {

    }

    public void onMatchEnd(ServerLevel level) {

    }

    public void onMatchStart(ServerLevel level) {

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
    public int modifyEntityColor(Entity entity, int color) {
        return color;
    }

    @OnlyIn(Dist.CLIENT)
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
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
            @SuppressWarnings("unchecked") T value = (T) GamemodeRegistry.getGamemode(getRegistryID());
            Objects.requireNonNull(value).decodeData(buffer);
            return value;
        });
    }

    @Override
    public @NotNull Type<ArenaGamemode> type() {
        return new Type<>(getRegistryID());
    }

    public void registerPacket(PayloadRegistrar registrar) {
        registrar.playToClient(type(), getStreamCodec(), ArenaGamemode::handleClientPacket);
    }
}
