package dev.entropy159.arena.core.blocks;

import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.core.items.TeamGemItem;
import dev.entropy159.entropylib.util.HasBarrierParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class KillBarrierBlock extends BarrierBlock implements HasBarrierParticle {
    public KillBarrierBlock() {
        super(Properties.ofFullCopy(Blocks.BARRIER));
    }

    @Override
    public void stepOn(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level instanceof ServerLevel serverLevel) {
            ArenaData data = ArenaData.get(serverLevel);
            if (data.inGame()) {
                entity.lavaHurt();
                entity.hurt(entity.damageSources().fellOutOfWorld(), 1000000);
                if ((entity instanceof ItemEntity itemEntity) && (itemEntity.getItem().getItem() instanceof TeamGemItem flag)) {
                    flag.reset(serverLevel, itemEntity.getItem());
                }
            }
        }
    }
}
