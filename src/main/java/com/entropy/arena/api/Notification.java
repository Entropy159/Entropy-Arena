package com.entropy.arena.api;

import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.api.client.ScreenAnchorPoint;
import com.entropy.arena.core.config.ClientConfig;
import com.entropy.arena.core.network.toClient.NotificationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public record Notification(Component message, long timestamp) {
    public static void toAll(Component message) {
        PacketDistributor.sendToAllPlayers(new NotificationPacket(message));
    }

    public static void toPlayer(Component message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new NotificationPacket(message));
    }

    public boolean tryRender(GuiGraphics graphics, ScreenAnchorPoint anchor) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        if (expired(level.getGameTime())) {
            return false;
        }
        ArenaRenderingUtils.renderTextWithAlpha(graphics, message, anchor, getAlpha(level.getGameTime()));
        return true;
    }

    private boolean expired(long now) {
        double delay = ClientConfig.NOTIFICATION_FADEOUT_DELAY.get() * 20;
        double duration = ClientConfig.NOTIFICATION_FADEOUT_DURATION.get() * 20;
        return timestamp + delay + duration <= now;
    }

    private float getAlpha(long now) {
        if (expired(now)) {
            return 0;
        }
        double delay = ClientConfig.NOTIFICATION_FADEOUT_DELAY.get() * 20;
        double duration = ClientConfig.NOTIFICATION_FADEOUT_DURATION.get() * 20;
        if (timestamp + delay > now) {
            return 1;
        }
        return (float) Math.clamp(1 - (now - (timestamp + delay)) / duration, 0, 1);
    }
}
