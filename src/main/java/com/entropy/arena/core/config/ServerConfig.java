package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CONCURRENT_CHUNK_LOADS = BUILDER.comment("The maximum chunks that can be processed at a time for backups").defineInRange("concurrentChunkLoads", 5, 1, 50);
    public static final ModConfigSpec.IntValue INTERVAL_SECONDS = BUILDER.comment("The duration in seconds of the interval between rounds").defineInRange("intervalSeconds", 30, 5, 120);
    public static final ModConfigSpec.IntValue RECAP_SECONDS = BUILDER.comment("The duration in seconds before starting map voting").defineInRange("recapSeconds", 5, 0, 100);
    public static final ModConfigSpec.IntValue DEFAULT_ROUND_SECONDS = BUILDER.comment("The default duration in seconds for timed rounds to last").defineInRange("defaultRoundSeconds", 600, 15, 1800);
    public static final ModConfigSpec.IntValue DEFAULT_TARGET_SCORE = BUILDER.comment("The default target score for score rounds").defineInRange("defaultTargetScore", 15, 1, 1000);
    public static final ModConfigSpec.BooleanValue FRIENDLY_FIRE = BUILDER.comment("Whether players on the same team can damage each other (and themselves)").define("friendlyFire", false);
    public static final ModConfigSpec.BooleanValue HIDE_ENEMY_NAMETAGS = BUILDER.comment("Whether nametags from other teams should be hidden").define("hideEnemyNametags", true);
    public static final ModConfigSpec.IntValue RESPAWN_DELAY = BUILDER.comment("The duration in seconds of spectator mode before respawning").defineInRange("respawnDelay", 5, 0, 30);
    public static final ModConfigSpec.BooleanValue GIVE_SATURATION = BUILDER.comment("Whether to keep players at full saturation during rounds").define("giveSaturation", true);
    public static final ModConfigSpec.BooleanValue INFINITE_BLOCKS = BUILDER.comment("Whether team blocks should be infinite").define("infiniteBlocks", true);
    public static final ModConfigSpec.IntValue SPAWN_PROTECTION = BUILDER.comment("The duration in seconds where players cannot be damaged after respawning").defineInRange("spawnProtection", 5, 0, 15);
    public static final ModConfigSpec.IntValue MAX_HEALTH = BUILDER.comment("The max player health").defineInRange("maxHealth", 20, 1, 10000);
    public static final ModConfigSpec.BooleanValue PREVENT_BLOCKS_ON_SPAWNS = BUILDER.comment("Whether to prevent placing blocks on top of spawns").define("preventBlocksOnSpawns", true);

    public static final ModConfigSpec.BooleanValue DEDUCT_POINTS_ON_SELF_DEATH = BUILDER.comment("If enabled, a self-kill (or a team kill) will subtract a point from your score").define("deductPointsOnSelfDeath", true);

    public static final ModConfigSpec.IntValue KILL_STREAK_LOSE_ANNOUNCE = BUILDER.comment("The minimum kill streak needed before announcing a loss").defineInRange("killStreakLoseAnnounce", 3, 0, 100);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> KILL_STREAK_ANNOUNCEMENTS = BUILDER.comment("The kill streak count and respective messages to announce", "Follows the format 'count: message where %s is the player name'").defineListAllowEmpty("killStreakAnnouncements", new ArrayList<>() {{
        add("3: %s has a 3 kill streak");
        add("5: %s is on a roll with 5 kills");
        add("10: %s is dominating with 10 kills");
    }}, () -> "0: %s has a %s kill streak", obj -> obj instanceof String string && string.matches("[0-9]*: .*"));

    public static final ModConfigSpec.BooleanValue REQUIRE_GEM_TO_SCORE = BUILDER.comment("Whether scoring on a pedestal requires that pedestal's gem to be present").define("ctf.requireGemToScore", false);
    public static final ModConfigSpec.IntValue TEAM_SWITCH_COOLDOWN = BUILDER.comment("The duration in seconds to prevent switching teams again").defineInRange("ctf.teamSwitchCooldown", 15, 0, 120);
    public static final ModConfigSpec.BooleanValue GLOWING_FOR_FLAG = BUILDER.comment("Whether to use glowing for flag locations instead of ping icons").define("ctf.glowingForFlag", false);
    public static final ModConfigSpec.BooleanValue RETURN_ALL_GEMS = BUILDER.comment("Whether clicking a pedestal will score/return all gems instead of just the held one").define("ctf.returnAllGems", true);
    public static final ModConfigSpec.IntValue FLAG_EXPIRATION_SECONDS = BUILDER.comment("How many seconds a flag can be on the ground before getting returned").defineInRange("ctf.flagExpirationSeconds", 120, 0, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
