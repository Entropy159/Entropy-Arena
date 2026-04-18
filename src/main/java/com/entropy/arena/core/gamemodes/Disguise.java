package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.api.loadout.Loadout;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.items.DisguiseItem;
import com.tterrag.registrate.Registrate;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.UUID;

public class Disguise extends TeamGamemode {
    private static final StreamCodec<ByteBuf, HashMap<UUID, BlockState>> DISGUISE_MAP_CODEC = ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));
    private HashMap<UUID, BlockState> disguiseMap = new HashMap<>();

    @Override
    public ResourceLocation getRegistryID() {
        return EntropyArena.id("disguise");
    }

    @Override
    public Registrate getRegistrate() {
        return EntropyArena.REGISTRATE;
    }

    @Override
    public void generateLang() {
        setNameTranslation("Disguise");
    }

    public void setDisguise(ServerPlayer player, BlockState disguise) {
        if (getPlayerTeam(player) == ArenaTeam.RED) return;
        disguiseMap.put(player.getUUID(), disguise);
        player.refreshDimensions();
        sendToAll();
    }

    public BlockState getDisguise(Player player) {
        return disguiseMap.get(player.getUUID());
    }

    @Override
    public void onPlayerHurt(ServerPlayer player, DamageSource source) {
        if (source.getDirectEntity() instanceof ServerPlayer) {
            disguiseMap.remove(player.getUUID());
            player.refreshDimensions();
            sendToAll();
        }
    }

    @Override
    public boolean isValidLoadout(ServerPlayer player, Loadout loadout) {
        return getPlayerTeam(player) == ArenaTeam.RED ? super.isValidLoadout(player, loadout) : loadout.contains(player.serverLevel(), stack -> stack.getItem() instanceof DisguiseItem);
    }

    @Override
    public boolean onDeath(ServerPlayer player, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer killer && getPlayerTeam(killer) == ArenaTeam.RED) {
            incrementScore(ArenaTeam.RED);
        }
        return super.onDeath(player, source);
    }

    @Override
    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        DISGUISE_MAP_CODEC.encode(buffer, disguiseMap);
    }

    @Override
    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        disguiseMap = DISGUISE_MAP_CODEC.decode(buffer);
        refreshDimensions();
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshDimensions() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.refreshDimensions();
        }
    }
}
