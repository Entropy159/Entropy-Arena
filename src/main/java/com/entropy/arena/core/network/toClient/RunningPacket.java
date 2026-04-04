package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.client.MusicData;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RunningPacket(boolean running, boolean lobby) implements CustomPacketPayload {
    public static final Type<RunningPacket> TYPE = new Type<>(EntropyArena.id("running"));
    public static final StreamCodec<ByteBuf, RunningPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, RunningPacket::running, ByteBufCodecs.BOOL, RunningPacket::lobby, RunningPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ClientData.inLobby != lobby || ClientData.running != running) {
            MusicData.nextMusic = true;
        }
        ClientData.running = running;
        ClientData.inLobby = lobby;
        if (!lobby || !running) {
            ClientData.votableMaps.clear();
        }
    }

    public static void sendToEveryone(ArenaData data) {
        PacketDistributor.sendToAllPlayers(new RunningPacket(data.running, data.lobby));
    }

    public static void sendToPlayer(ArenaData data, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new RunningPacket(data.running, data.lobby));
    }
}
