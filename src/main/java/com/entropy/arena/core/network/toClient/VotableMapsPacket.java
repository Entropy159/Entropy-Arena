package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.ArenaGameType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record VotableMapsPacket(List<ArenaMapInfo> maps,
                                Map<ArenaGameType, Integer> typeVotes, boolean force) implements CustomPacketPayload {
    public static final Type<VotableMapsPacket> TYPE = new Type<>(EntropyArena.id("votable_maps"));
    public static final StreamCodec<ByteBuf, VotableMapsPacket> STREAM_CODEC = StreamCodec.composite(ArenaMapInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), VotableMapsPacket::maps, ByteBufCodecs.map(HashMap::new, ArenaGameType.STREAM_CODEC, ByteBufCodecs.INT), VotableMapsPacket::typeVotes, ByteBufCodecs.BOOL, VotableMapsPacket::force, VotableMapsPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.votableMaps = maps;
        ClientData.typeVotes = typeVotes;
        EntropyArenaClient.openVotingScreen(force);
    }
}
