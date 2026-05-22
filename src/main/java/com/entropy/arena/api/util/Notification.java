package com.entropy.arena.api.util;

import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.api.client.ScreenAnchorPoint;
import com.entropy.arena.core.config.ClientConfig;
import com.entropy.arena.core.network.toClient.NotificationPacket;
import net.minecraft.Util;
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
        if (expired()) {
            return false;
        }
        ArenaRenderingUtils.renderTextWithAlpha(graphics, message, anchor, getAlpha());
        return true;
    }

    private boolean expired() {
        double delay = ClientConfig.NOTIFICATION_FADEOUT_DELAY.get() * 1000;
        double duration = ClientConfig.NOTIFICATION_FADEOUT_DURATION.get() * 1000;
        return timestamp + delay + duration <= Util.getMillis();
    }

    private float getAlpha() {
        if (expired()) {
            return 0;
        }
        double delay = ClientConfig.NOTIFICATION_FADEOUT_DELAY.get() * 1000;
        double duration = ClientConfig.NOTIFICATION_FADEOUT_DURATION.get() * 1000;
        if (timestamp + delay > Util.getMillis()) {
            return 1;
        }
        return (float) Math.clamp(1 - (Util.getMillis() - (timestamp + delay)) / duration, 0, 1);
    }
}
