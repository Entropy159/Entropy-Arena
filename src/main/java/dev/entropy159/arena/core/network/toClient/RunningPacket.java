package dev.entropy159.arena.core.network.toClient;

import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.util.ArenaGameType;
import dev.entropy159.arena.client.MusicData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.config.ServerConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RunningPacket(boolean running, boolean lobby, int targetScore,
                            ArenaGameType gameType) implements CustomPacketPayload {
    public static final Type<RunningPacket> TYPE = new Type<>(EntropyArena.id("running"));
    public static final StreamCodec<ByteBuf, RunningPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, RunningPacket::running, ByteBufCodecs.BOOL, RunningPacket::lobby, ByteBufCodecs.INT, RunningPacket::targetScore, ArenaGameType.STREAM_CODEC, RunningPacket::gameType, RunningPacket::new);

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
        ClientData.gameType = gameType;
        ClientData.targetScore = targetScore;
        if (!lobby || !running) {
            ClientData.votableMaps.clear();
        }
    }

    public static RunningPacket fromData(MinecraftServer server) {
        ArenaData data = ArenaData.get(server);
        return new RunningPacket(data.running, data.lobby, data.currentMap == null ? 0 : ServerConfig.TARGET_SCORE.get(), data.gameType);
    }
}
