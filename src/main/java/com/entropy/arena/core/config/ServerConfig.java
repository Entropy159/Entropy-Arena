package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

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
    public static final ModConfigSpec.BooleanValue INFINITE_BLOCKS = BUILDER.define("infiniteBlocks", true);
    public static final ModConfigSpec.IntValue SPAWN_PROTECTION = BUILDER.defineInRange("spawnProtection", 5, 0, 15);
    public static final ModConfigSpec.IntValue MAX_HEALTH = BUILDER.defineInRange("maxHealth", 20, 1, 10000);

    public static final ModConfigSpec.IntValue KILL_STREAK_LOSE_ANNOUNCE = BUILDER.defineInRange("killStreakLoseAnnounce", 3, 0, 100);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> KILL_STREAK_ANNOUNCEMENTS = BUILDER.defineListAllowEmpty("killStreakAnnouncements", new ArrayList<>() {{
        add("3: %s has a 3 kill streak");
        add("5: %s is on a roll with 5 kills");
        add("10: %s is dominating with 10 kills");
    }}, () -> "0: %s has a %s kill streak", obj -> obj instanceof String string && string.matches("[0-9]*: .*"));

    public static final ModConfigSpec.BooleanValue REQUIRE_GEM_TO_SCORE = BUILDER.define("ctf.requireGemToScore", false);
    public static final ModConfigSpec.IntValue TEAM_SWITCH_COOLDOWN = BUILDER.defineInRange("ctf.teamSwitchCooldown", 15, 0, 120);
    public static final ModConfigSpec.BooleanValue GLOWING_FOR_FLAG = BUILDER.define("ctf.glowingForFlag", false);
    public static final ModConfigSpec.BooleanValue RETURN_ALL_GEMS = BUILDER.define("ctf.returnAllGems", true);
    public static final ModConfigSpec.IntValue FLAG_EXPIRATION_SECONDS = BUILDER.defineInRange("ctf.flagExpirationSeconds", 120, 0, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
