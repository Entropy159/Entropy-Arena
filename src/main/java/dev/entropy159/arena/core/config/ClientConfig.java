package dev.entropy159.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DELAY = BUILDER.defineInRange("notificationFadeoutDelay", 5d, 1, 30);
    public static final ModConfigSpec.DoubleValue NOTIFICATION_FADEOUT_DURATION = BUILDER.defineInRange("notificationFadeoutDuration", 1d, 0, 5);
    public static final ModConfigSpec.BooleanValue USE_CHAT_FOR_NOTIFICATIONS = BUILDER.comment("If enabled, notifications will be sent to the chat instead of the custom feed").define("useChatForNotifications", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
