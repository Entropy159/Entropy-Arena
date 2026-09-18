package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.ui.loadout.LoadoutSelectionUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenLoadoutsPacket() implements CustomPacketPayload {
    public static final Type<OpenLoadoutsPacket> TYPE = new Type<>(EntropyArena.id("loadout_menu"));
    public static final StreamCodec<ByteBuf, OpenLoadoutsPacket> STREAM_CODEC = StreamCodec.unit(new OpenLoadoutsPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            LoadoutSelectionUI.open(player);
        }
    }
}
