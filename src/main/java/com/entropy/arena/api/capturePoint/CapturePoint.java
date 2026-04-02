package com.entropy.arena.api.capturePoint;

import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public abstract class CapturePoint {
    private BlockPos pos;
    private float captureProgress;
    private boolean contested;
    private int captureTimer;

    public CapturePoint(BlockPos pos) {
        this(pos, 0, 0, false);
    }

    public CapturePoint(BlockPos pos, float captureProgress, int captureTimer, boolean contested) {
        this.pos = pos;
        this.captureProgress = captureProgress;
        this.contested = contested;
        this.captureTimer = captureTimer;
    }

    public CapturePoint(CapturePoint other) {
        this(other.pos, other.captureProgress, other.captureTimer, other.contested);
    }

    public int getCaptureRadius() {
        return 5;
    }

    public int getCaptureDelayTicks() {
        return 40;
    }

    public float getCaptureIncrement() {
        return 0.2f;
    }

    public void onLevelTick(ServerLevel level) {
        List<ServerPlayer> contestants = getPlayersInRadius(level);
        if (contestants.isEmpty()) {
            setContested(false);
        } else if (contestants.size() == 1) {
            setContested(false);
        } else {
            boolean contested = false;
            for (ServerPlayer one : contestants) {
                for (ServerPlayer two : contestants) {
                    contested = contested || !playersAreCompatible(one, two);
                }
            }
            if (contested) {
                contestants.forEach(player -> player.displayClientMessage(Component.translatable("arena.message.capture_point_contested"), true));
            }
            setContested(contested);
            resetCaptureProgress();
        }
    }

    public boolean playersAreCompatible(ServerPlayer one, ServerPlayer two) {
        return one == two;
    }

    public boolean isWithinDistance(Vec3 pos) {
        return pos.closerThan(getPos().getCenter(), getCaptureRadius());
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean tryIncrementCapture(ServerLevel level) {
        getPlayersInRadius(level).forEach(player -> player.displayClientMessage(getCaptureProgressText(), true));
        if (incrementCaptureTimer()) {
            return incrementCaptureProgress();
        }
        return false;
    }

    public boolean incrementCaptureTimer() {
        captureTimer--;
        if (captureTimer <= 0) {
            captureTimer = getCaptureDelayTicks();
            return true;
        }
        return false;
    }

    public boolean incrementCaptureProgress() {
        captureProgress += getCaptureIncrement();
        if (captureProgress >= 1) {
            captureProgress = 1;
            return true;
        }
        return false;
    }

    public boolean isBeingTaken() {
        return captureProgress > 0 && captureProgress < 1;
    }

    public float getCaptureProgress() {
        return captureProgress;
    }

    public boolean isContested() {
        return contested;
    }

    public void setContested(boolean contested) {
        this.contested = contested;
    }

    public int getCaptureTimer() {
        return captureTimer;
    }

    public void resetCaptureProgress() {
        captureProgress = 0;
        captureTimer = 0;
    }

    public List<ServerPlayer> getPlayersInRadius(ServerLevel level) {
        return level.players().stream().filter(player -> !player.isSpectator() && isWithinDistance(player.position())).toList();
    }

    public Component getCaptureProgressText() {
        return captureProgress >= 1 ? Component.translatable("arena.message.capture_point_holding").withStyle(ChatFormatting.GREEN) : Component.translatable("arena.message.capture_point_progress", (int) (captureProgress * 100)).withStyle(ChatFormatting.YELLOW);
    }

    public void encodeData(ByteBuf buffer) {
        BlockPos.STREAM_CODEC.encode(buffer, pos);
        ByteBufCodecs.FLOAT.encode(buffer, captureProgress);
        ByteBufCodecs.INT.encode(buffer, captureTimer);
        ByteBufCodecs.BOOL.encode(buffer, contested);
    }

    public void decode(ByteBuf buffer) {
        pos = BlockPos.STREAM_CODEC.decode(buffer);
        captureProgress = ByteBufCodecs.FLOAT.decode(buffer);
        captureTimer = ByteBufCodecs.INT.decode(buffer);
        contested = ByteBufCodecs.BOOL.decode(buffer);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, DeltaTracker tracker) {
        ArenaRenderingUtils.renderImageAtWorldPos(graphics, getIcon(), getPos().getCenter(), 16, getColor(tracker));
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getIcon() {
        return EntropyArena.id(isContested() ? "capture_point_contested" : "capture_point");
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLocalPlayerInPoint() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        return isWithinDistance(client.player.position());
    }

    @OnlyIn(Dist.CLIENT)
    public abstract int getColor(DeltaTracker tracker);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CapturePoint point && pos.equals(point.pos);
    }
}
