package com.entropy.arena.core.blocks;

import com.entropy.arena.api.data.ArenaLogic;
import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.gamemodes.CaptureTheFlag;
import com.entropy.arena.core.items.TeamGemItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class PedestalBlock extends Block {
    public static final Property<ArenaTeam> GEM_COLOR = EnumProperty.create("gem_color", ArenaTeam.class);
    public static final Property<Boolean> HAS_GEM = BooleanProperty.create("has_gem");

    private static final int UPDATE_FLAGS = Block.UPDATE_ALL_IMMEDIATE;

    public PedestalBlock() {
        super(Properties.of().noOcclusion().lightLevel(state -> state.getValue(HAS_GEM) ? 10 : 2));
        registerDefaultState(getStateDefinition().any().setValue(GEM_COLOR, ArenaTeam.NONE).setValue(HAS_GEM, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(GEM_COLOR, HAS_GEM);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level l, @NotNull BlockPos pos, @NotNull Player p, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (l instanceof ServerLevel level && p instanceof ServerPlayer player) {
            ArenaTeam stackColor = ArenaTeam.getFromStack(stack);
            if (stackColor != null) {
                ArenaLogic data = ArenaLogic.get(level);
                if (data.inGame() && data.getCurrentGamemode() instanceof CaptureTheFlag ctf) {
                    if (stack.getItem() instanceof TeamGemItem) {
                        if (stackColor == state.getValue(GEM_COLOR)) {
                            if (!state.getValue(HAS_GEM)) ctf.returnGem(player, pos, stack);
                        } else {
                            ctf.scoreGem(player, pos, stack);
                        }
                    }
                } else {
                    level.setBlock(pos, state.setValue(GEM_COLOR, stackColor), UPDATE_FLAGS);
                }
            }
        }
        return super.useItemOn(stack, state, l, pos, p, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level l, @NotNull BlockPos pos, @NotNull Player p, @NotNull BlockHitResult hitResult) {
        if (l instanceof ServerLevel level && p instanceof ServerPlayer player) {
            ArenaLogic data = ArenaLogic.get(level);
            if (data.inGame() && data.getCurrentGamemode() instanceof CaptureTheFlag ctf) {
                if (state.getValue(HAS_GEM)) {
                    ctf.takeGem(player, pos);
                }
            }
        }
        return super.useWithoutItem(state, l, pos, p, hitResult);
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getValue(HAS_GEM) ? 15 : 0;
    }

    public static void setHasGem(ServerLevel level, BlockPos pos, boolean hasGem) {
        level.setBlock(pos, level.getBlockState(pos).setValue(HAS_GEM, hasGem), UPDATE_FLAGS);
    }
}
