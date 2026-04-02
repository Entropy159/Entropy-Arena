package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaLogic;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record GameInfoPacket(String mapName, ResourceLocation gamemode) implements CustomPacketPayload {
    public static final Type<GameInfoPacket> TYPE = new Type<>(EntropyArena.id("game_info"));
    public static final StreamCodec<ByteBuf, GameInfoPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, GameInfoPacket::mapName, ResourceLocation.STREAM_CODEC, GameInfoPacket::gamemode, GameInfoPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static GameInfoPacket fromData(ArenaLogic data) {
        return new GameInfoPacket(data.getCurrentMap() == null ? "" : data.getCurrentMap().getName(), data.getCurrentGamemode() == null ? GamemodeRegistry.NONE_ID : data.getCurrentGamemode().getRegistryID());
    }

    public void handle(IPayloadContext ctx) {
        ClientData.currentMap = mapName.isBlank() ? null : mapName;
        ClientData.currentGamemode = GamemodeRegistry.getGamemode(gamemode);
    }
}
