package com.entropy.arena.core.network.toServer;

import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.MapList;
import com.entropy.arena.api.map.MapScreenshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ScreenshotPacket(MapScreenshot screenshot) implements CustomPacketPayload {
    public static final Type<ScreenshotPacket> TYPE = new Type<>(EntropyArena.id("screenshot"));
    public static final StreamCodec<ByteBuf, ScreenshotPacket> STREAM_CODEC = StreamCodec.composite(MapScreenshot.STREAM_CODEC, ScreenshotPacket::screenshot, ScreenshotPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ArenaMap map = MapList.getMap(screenshot.getMapName());
        if (map != null) {
            map.setScreenshot(screenshot);
        }
    }
}
