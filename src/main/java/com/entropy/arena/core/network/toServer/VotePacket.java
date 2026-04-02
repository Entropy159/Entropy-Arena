package com.entropy.arena.core.network.toServer;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record VotePacket(String mapName) implements CustomPacketPayload {
    public static final Type<VotePacket> TYPE = new Type<>(EntropyArena.id("vote"));
    public static final StreamCodec<ByteBuf, VotePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, VotePacket::mapName, VotePacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ArenaData data = ArenaData.get(player.serverLevel());
            data.vote(player, mapName);
        }
    }
}
