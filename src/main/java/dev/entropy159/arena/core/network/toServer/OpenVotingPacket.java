package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.voting.MapVotingUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenVotingPacket() implements CustomPacketPayload {
    public static final Type<OpenVotingPacket> TYPE = new Type<>(EntropyArena.id("open_voting"));
    public static final StreamCodec<ByteBuf, OpenVotingPacket> STREAM_CODEC = StreamCodec.unit(new OpenVotingPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            MapVotingUI.open(player, ArenaData.get(player.getServer()));
        }
    }
}
