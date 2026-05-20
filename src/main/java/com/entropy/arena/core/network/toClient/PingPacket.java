package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.client.PingIcon;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record PingPacket(Vector3f pos, int color) implements CustomPacketPayload {
    public static final Type<PingPacket> TYPE = new Type<>(EntropyArena.id("ping"));
    public static final StreamCodec<ByteBuf, PingPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VECTOR3F, PingPacket::pos, ByteBufCodecs.INT, PingPacket::color, PingPacket::new);

    public PingPacket(Vec3 pos, int color) {
        this(pos.toVector3f(), color);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (Minecraft.getInstance().level != null) {
            ClientData.pings.add(new PingIcon(pos, color, Util.getMillis()));
        }
    }
}
