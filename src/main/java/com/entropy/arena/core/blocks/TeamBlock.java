package com.entropy.arena.core.blocks;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.config.CommonConfig;
import com.entropy.arena.core.registry.ArenaBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamBlock extends Block {
    public TeamBlock() {
        super(Properties.ofFullCopy(Blocks.BLACK_WOOL));
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params) {
        return CommonConfig.INFINITE_BLOCKS.get() ? List.of() : super.getDrops(state, params);
    }

    public static ItemStack getStack(ArenaTeam team) {
        return new ItemStack(ArenaBlocks.TEAM_BLOCKS.get(team), CommonConfig.INFINITE_BLOCKS.get() ? 1 : 64);
    }
}
