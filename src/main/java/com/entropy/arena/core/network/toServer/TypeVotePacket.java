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

public record TypeVotePacket(boolean isTimed) implements CustomPacketPayload {
    public static final Type<TypeVotePacket> TYPE = new Type<>(EntropyArena.id("type_vote"));
    public static final StreamCodec<ByteBuf, TypeVotePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TypeVotePacket::isTimed, TypeVotePacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ArenaLogic.get(player.serverLevel()).voteForType(player, isTimed);
        }
    }
}
