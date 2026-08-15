package dev.entropy159.arena.core.network.toClient;

import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TimerPacket(int seconds) implements CustomPacketPayload {
    public static final Type<TimerPacket> TYPE = new Type<>(EntropyArena.id("update_timer"));
    public static final StreamCodec<ByteBuf, TimerPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, TimerPacket::seconds, TimerPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.timer = seconds;
    }
}
