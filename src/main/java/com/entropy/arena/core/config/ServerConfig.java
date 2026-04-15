package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CONCURRENT_CHUNK_LOADS = BUILDER.defineInRange("concurrentChunkLoads", 5, 1, 50);
    public static final ModConfigSpec.IntValue INTERVAL_SECONDS = BUILDER.defineInRange("intervalSeconds", 30, 5, 120);
    public static final ModConfigSpec.IntValue RECAP_SECONDS = BUILDER.defineInRange("recapSeconds", 5, 0, 100);
    public static final ModConfigSpec.IntValue DEFAULT_ROUND_SECONDS = BUILDER.defineInRange("defaultRoundSeconds", 600, 15, 1800);
    public static final ModConfigSpec.IntValue DEFAULT_TARGET_SCORE = BUILDER.defineInRange("defaultTargetScore", 15, 1, 1000);
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE = BUILDER.define("friendlyFire", false);
    public static final ModConfigSpec.BooleanValue HIDE_ENEMY_NAMETAGS = BUILDER.define("hideEnemyNametags", true);
    public static final ModConfigSpec.IntValue RESPAWN_DELAY = BUILDER.defineInRange("respawnDelay", 5, 0, 30);
    public static final ModConfigSpec.BooleanValue GIVE_SATURATION = BUILDER.define("giveSaturation", true);
    public static final ModConfigSpec.BooleanValue RETURN_ALL_GEMS = BUILDER.define("returnAllGems", true);
    public static final ModConfigSpec.IntValue FLAG_EXPIRATION_SECONDS = BUILDER.defineInRange("flagExpirationSeconds", 60, 0, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
