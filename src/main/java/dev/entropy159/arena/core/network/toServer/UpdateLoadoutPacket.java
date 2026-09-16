package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateLoadoutPacket(String name, Loadout loadout) implements CustomPacketPayload {
    public static final Type<UpdateLoadoutPacket> TYPE = new Type<>(EntropyArena.id("update_loadout"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLoadoutPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, UpdateLoadoutPacket::name, Loadout.STREAM_CODEC, UpdateLoadoutPacket::loadout, UpdateLoadoutPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            ArenaLogic.get(player.getServer()).updateLoadout(name, loadout);
        }
    }
}
