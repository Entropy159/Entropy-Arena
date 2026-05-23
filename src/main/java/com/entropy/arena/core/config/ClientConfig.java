package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DELAY = BUILDER.defineInRange("notificationFadeoutDelay", 5d, 1, 30);
    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DURATION = BUILDER.defineInRange("notificationFadeoutDuration", 1d, 0, 5);
    public static final ModConfigSpec.BooleanValue USE_CHAT_FOR_NOTIFICATIONS = BUILDER.define("useChatForNotifications", true);
    public static final ModConfigSpec.DoubleValue ICON_FADE_RADIUS = BUILDER.comment("The radius from the center of the screen to fade icons in", "A value of 0.5 means half the distance from the edge to the center").defineInRange("iconFadeRadius", 0.1, 0, 1);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
