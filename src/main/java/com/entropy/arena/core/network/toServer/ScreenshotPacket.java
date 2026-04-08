package com.entropy.arena.core.network.toServer;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.MapScreenshot;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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
        if (ctx.player() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(player.serverLevel());
            ArenaMap map = data.mapList.getMap(screenshot.getMapName());
            if (map != null) {
                map.setScreenshot(screenshot);
            }
        }
    }
}
