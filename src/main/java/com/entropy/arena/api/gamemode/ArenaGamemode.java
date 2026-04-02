package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaLogic;
import com.entropy.arena.api.gear.StarterGear;
import com.entropy.arena.core.registry.ArenaDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class ArenaGamemode implements CustomPacketPayload {
    private final ResourceLocation id;

    public ArenaGamemode(ResourceLocation id) {
        this.id = id;
    }

    public Component getName() {
        return Component.translatable("arena.gamemode." + id.toLanguageKey());
    }

    public ResourceLocation getRegistryID() {
        return id;
    }

    public abstract void generateLang();

    public void onLevelTick(ArenaLogic data) {

    }

    public void onEntityTick(ArenaLogic data, Entity entity) {

    }

    /**
     * This method is called when the player dies. It drops items that either have
     * the SHOULD_DROP_ON_DEATH data component, or returns true from shouldDropOnDeath()
     *
     * @param data   The ArenaData object the method is called from
     * @param player The player that died
     * @param source The damage source that caused the player to die
     * @return Whether to prevent the player from dying or not
     */
    public boolean onDeath(ArenaLogic data, ServerPlayer player, DamageSource source) {
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

    public void modifyStarterGear(StarterGear gear) {
    }

    public void onJoin(ArenaLogic data, ServerPlayer player) {
        sendToPlayer(player);
    }

    public void onLeave(ArenaLogic data, ServerPlayer player) {

    }

    public void onGameEnd(ArenaLogic data) {

    }

    public void onGameStart(ArenaLogic data) {

    }

    public ArenaTeam getTeamForBlock(ServerPlayer player) {
        return ArenaTeam.NONE;
    }

    public abstract int getHighestScore();

    public abstract List<Component> getScoreText(ServerLevel level);

    public abstract ArrayList<BlockPos> getValidSpawns(ArenaLogic data, ServerPlayer player);

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
