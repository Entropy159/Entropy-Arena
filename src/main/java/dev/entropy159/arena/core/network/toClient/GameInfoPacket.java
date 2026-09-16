package dev.entropy159.arena.core.network.toClient;

import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record GameInfoPacket(Optional<ArenaMap> map, ResourceLocation gamemode) implements CustomPacketPayload {
    public static final Type<GameInfoPacket> TYPE = new Type<>(EntropyArena.id("game_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GameInfoPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ArenaMap.STREAM_CODEC), GameInfoPacket::map, ResourceLocation.STREAM_CODEC, GameInfoPacket::gamemode, GameInfoPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static GameInfoPacket fromData(ArenaData data) {
        return new GameInfoPacket(Optional.ofNullable(data.currentMap), data.currentGamemode == null ? GamemodeRegistry.NONE_ID : data.currentGamemode.getRegistryID());
    }

    public void handle(IPayloadContext ctx) {
        ClientData.currentMap = map.orElse(null);
        ClientData.currentGamemode = GamemodeRegistry.getNew(gamemode);
    }
}
