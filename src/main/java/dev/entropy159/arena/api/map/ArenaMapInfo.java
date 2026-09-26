package dev.entropy159.arena.api.map;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ArenaMapInfo(String name, MapScreenshot screenshot, ResourceLocation gamemode, Vec3i size, int votes) {
    public static final StreamCodec<ByteBuf, ArenaMapInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ArenaMapInfo::name, MapScreenshot.STREAM_CODEC, ArenaMapInfo::screenshot, ResourceLocation.STREAM_CODEC, ArenaMapInfo::gamemode, StreamCodec.of((buf, val) -> {
        buf.writeInt(val.getX());
        buf.writeInt(val.getY());
        buf.writeInt(val.getZ());
    }, buf -> new Vec3i(buf.readInt(), buf.readInt(), buf.readInt())), ArenaMapInfo::size, ByteBufCodecs.INT, ArenaMapInfo::votes, ArenaMapInfo::new);
}
