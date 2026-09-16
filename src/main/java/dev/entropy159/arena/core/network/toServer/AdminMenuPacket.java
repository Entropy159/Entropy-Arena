package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.OverviewUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AdminMenuPacket() implements CustomPacketPayload {
    public static final Type<AdminMenuPacket> TYPE = new Type<>(EntropyArena.id("admin_menu"));
    public static final StreamCodec<ByteBuf, AdminMenuPacket> STREAM_CODEC = StreamCodec.unit(new AdminMenuPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            OverviewUI.open(player);
        }
    }
}
