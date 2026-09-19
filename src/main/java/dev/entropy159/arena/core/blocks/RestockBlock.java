package dev.entropy159.arena.core.blocks;

import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.events.RestockEvent;
import dev.entropy159.arena.core.config.ServerConfig;
import dev.entropy159.entropylib.util.EventScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

public class RestockBlock extends Block {
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    public RestockBlock() {
        super(Properties.ofFullCopy(Blocks.NETHERITE_BLOCK));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && state.getValue(ENABLED) && ArenaData.get(serverPlayer.getServer()).inGame()) {
            NeoForge.EVENT_BUS.post(new RestockEvent(serverPlayer, pos));
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS);
            level.setBlock(pos, state.setValue(ENABLED, false), Block.UPDATE_ALL_IMMEDIATE);
            EventScheduler.schedule(ServerConfig.RESTOCK_COOLDOWN.get(), () -> level.setBlock(pos, state.setValue(ENABLED, true), Block.UPDATE_ALL_IMMEDIATE));
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
