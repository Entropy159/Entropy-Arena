package dev.entropy159.arena.core.network.toClient;

import dev.entropy159.arena.api.util.Notification;
import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.client.EntropyArenaClient;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.config.ClientConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
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
            if (ClientConfig.USE_CHAT_FOR_NOTIFICATIONS.get()) {
                EntropyArenaClient.sendChatMessage(message);
            } else {
                ClientData.notifications.add(new Notification(message, Util.getMillis()));
                EntropyArena.LOGGER.info("[Notification] {}", message.getString());
            }
        }
    }
}
