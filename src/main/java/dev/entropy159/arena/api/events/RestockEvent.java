package dev.entropy159.arena.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RestockEvent extends PlayerEvent {
    private final BlockPos pos;

    public RestockEvent(ServerPlayer player, BlockPos pos) {
        super(player);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public @NotNull ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }
}
