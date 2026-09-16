package dev.entropy159.arena.core.network.toServer;

import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.api.loadout.LoadoutSerializerRegistry;
import dev.entropy159.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record LoadoutEditPacket(String name, boolean save) implements CustomPacketPayload {
    public static final Type<LoadoutEditPacket> TYPE = new Type<>(EntropyArena.id("loadout_edit"));
    public static final StreamCodec<ByteBuf, LoadoutEditPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, LoadoutEditPacket::name, ByteBufCodecs.BOOL, LoadoutEditPacket::save, LoadoutEditPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player && player.hasPermissions(2)) {
            var loadouts = ArenaData.get(player.server).loadouts;
            if (save) {
                loadouts.put(name, new Loadout(player));
                LoadoutSerializerRegistry.clearAll(player);
            } else {
                loadouts.get(name).giveToPlayer(player);
            }
            player.closeContainer();
        }
    }
}
