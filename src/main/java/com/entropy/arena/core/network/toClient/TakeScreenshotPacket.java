package com.entropy.arena.core.network.toClient;

import com.entropy.arena.client.EntropyArenaClient;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TakeScreenshotPacket(String mapName) implements CustomPacketPayload {
    public static final Type<TakeScreenshotPacket> TYPE = new Type<>(EntropyArena.id("take_screenshot"));
    public static final StreamCodec<ByteBuf, TakeScreenshotPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, TakeScreenshotPacket::mapName, TakeScreenshotPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        EntropyArenaClient.takeScreenshot(mapName);
    }
}
