package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.map.ArenaMapInfo;
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

public record VotableMapsPacket(List<ArenaMapInfo> maps) implements CustomPacketPayload {
    public static final Type<VotableMapsPacket> TYPE = new Type<>(EntropyArena.id("votable_maps"));
    public static final StreamCodec<ByteBuf, VotableMapsPacket> STREAM_CODEC = StreamCodec.composite(ArenaMapInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), VotableMapsPacket::maps, VotableMapsPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.votableMaps = new ArrayList<>(maps);
        EntropyArenaClient.openVotingScreen();
    }
}
