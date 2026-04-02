package com.entropy.arena.core.network.toClient;

import com.entropy.arena.api.Notification;
import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.config.ClientConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record NotificationPacket(Component message) implements CustomPacketPayload {
    public static final Type<NotificationPacket> TYPE = new Type<>(EntropyArena.id("notification"));
    public static final StreamCodec<ByteBuf, NotificationPacket> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, NotificationPacket::message, NotificationPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if (ClientConfig.CHAT_INSTEAD_OF_NOTIFICATION.get() && Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(message);
            } else {
                ClientData.notifications.add(new Notification(message, level.getGameTime()));
                EntropyArena.LOGGER.info("[Notification] {}", message.getString());
            }
        }
    }
}
