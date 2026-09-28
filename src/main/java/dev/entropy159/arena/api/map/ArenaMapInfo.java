package dev.entropy159.arena.api.map;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ArenaMapInfo(String name, MapScreenshot screenshot, ResourceLocation gamemode) {
    public static final StreamCodec<ByteBuf, ArenaMapInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ArenaMapInfo::name, MapScreenshot.STREAM_CODEC, ArenaMapInfo::screenshot, ResourceLocation.STREAM_CODEC, ArenaMapInfo::gamemode, ArenaMapInfo::new);
}
