package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateMapPacket(ArenaMap map) implements CustomPacketPayload {
    public static final Type<UpdateMapPacket> TYPE = new Type<>(EntropyArena.id("update_map"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMapPacket> STREAM_CODEC = StreamCodec.composite(ArenaMap.STREAM_CODEC, UpdateMapPacket::map, UpdateMapPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            ArenaLogic.get(player.getServer()).updateMap(map);
        }
    }
}
