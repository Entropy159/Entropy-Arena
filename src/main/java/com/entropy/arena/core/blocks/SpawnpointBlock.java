package com.entropy.arena.core.blocks;

import com.entropy.arena.api.ArenaTeam;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class SpawnpointBlock extends Block {
    public static final Property<ArenaTeam> SPAWN_COLOR = EnumProperty.create("spawn_color", ArenaTeam.class);

    public SpawnpointBlock() {
        super(Properties.of().noCollission());
        registerDefaultState(getStateDefinition().any().setValue(SPAWN_COLOR, ArenaTeam.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(SPAWN_COLOR);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        ArenaTeam color = ArenaTeam.getFromStack(stack);
        if (color != null) {
            level.setBlock(pos, state.setValue(SPAWN_COLOR, color), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
