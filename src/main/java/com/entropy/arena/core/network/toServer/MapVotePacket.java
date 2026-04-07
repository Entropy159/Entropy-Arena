package com.entropy.arena.core.network.toServer;

import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MapVotePacket(String mapName) implements CustomPacketPayload {
    public static final Type<MapVotePacket> TYPE = new Type<>(EntropyArena.id("map_vote"));
    public static final StreamCodec<ByteBuf, MapVotePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, MapVotePacket::mapName, MapVotePacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ArenaLogic.get(player.serverLevel()).voteForMap(player, mapName);
        }
    }
}
