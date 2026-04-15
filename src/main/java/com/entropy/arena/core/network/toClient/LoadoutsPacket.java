package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.client.EntropyArenaClient;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record LoadoutsPacket(List<String> loadouts) implements CustomPacketPayload {
    public static final Type<LoadoutsPacket> TYPE = new Type<>(EntropyArena.id("loadouts"));
    public static final StreamCodec<ByteBuf, LoadoutsPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), LoadoutsPacket::loadouts, LoadoutsPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.loadouts = new ArrayList<>(loadouts);
        EntropyArenaClient.openLoadoutScreen();
    }
}
