package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DELAY = BUILDER.defineInRange("notificationFadeoutDelay", 5d, 1, 30);
    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DURATION = BUILDER.defineInRange("notificationFadeoutDuration", 1d, 0, 5);
    public static final ModConfigSpec.BooleanValue CHAT_INSTEAD_OF_NOTIFICATION = BUILDER.define("useChatForNotifications", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
