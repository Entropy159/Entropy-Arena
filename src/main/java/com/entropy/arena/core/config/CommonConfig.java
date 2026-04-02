package com.entropy.arena.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue INFINITE_BLOCKS = BUILDER.define("infiniteBlocks", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
