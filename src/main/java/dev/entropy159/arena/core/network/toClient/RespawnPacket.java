package dev.entropy159.arena.core.network.toClient;

import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RespawnPacket(long gameTime) implements CustomPacketPayload {
    public static final Type<RespawnPacket> TYPE = new Type<>(EntropyArena.id("respawn"));
    public static final StreamCodec<ByteBuf, RespawnPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, RespawnPacket::gameTime, RespawnPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.lastRespawn = gameTime;
    }
}
