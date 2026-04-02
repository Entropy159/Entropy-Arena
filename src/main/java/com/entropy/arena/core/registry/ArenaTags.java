package com.entropy.arena.core.registry;

import com.entropy.arena.core.EntropyArena;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ArenaTags {
    public static final TagKey<Block> TEAM_BLOCK_INVALID = TagKey.create(Registries.BLOCK, EntropyArena.id("team_block_invalid"));
}
