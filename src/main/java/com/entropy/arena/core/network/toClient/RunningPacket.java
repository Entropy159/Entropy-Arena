package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.client.MusicData;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RunningPacket(boolean running, boolean lobby, int targetScore) implements CustomPacketPayload {
    public static final Type<RunningPacket> TYPE = new Type<>(EntropyArena.id("running"));
    public static final StreamCodec<ByteBuf, RunningPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, RunningPacket::running, ByteBufCodecs.BOOL, RunningPacket::lobby, ByteBufCodecs.INT, RunningPacket::targetScore, RunningPacket::new);

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
        ClientData.targetScore = targetScore;
        if (!lobby || !running) {
            ClientData.votableMaps.clear();
        }
    }

    public static RunningPacket fromData(ArenaData data) {
        return new RunningPacket(data.running, data.lobby, data.currentMap == null ? 0 : (data.gameType.isTimed() ? 0 : data.currentMap.getTargetScore()));
    }
}
