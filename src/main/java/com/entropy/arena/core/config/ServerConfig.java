package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue INTERVAL_SECONDS = BUILDER.defineInRange("intervalSeconds", 30, 5, 120);
    public static final ModConfigSpec.IntValue ROUND_SECONDS = BUILDER.defineInRange("roundSeconds", 600, 15, 1800);
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE = BUILDER.define("friendlyFire", false);
    public static final ModConfigSpec.BooleanValue HIDE_ENEMY_NAMETAGS = BUILDER.define("hideEnemyNametags", true);
    public static final ModConfigSpec.IntValue RESPAWN_DELAY = BUILDER.defineInRange("respawnDelay", 5, 0, 30);
    public static final ModConfigSpec.BooleanValue GIVE_SATURATION = BUILDER.define("giveSaturation", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
