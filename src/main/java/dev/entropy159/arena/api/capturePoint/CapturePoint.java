package dev.entropy159.arena.api.capturePoint;

import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.client.util.RenderingUtils;
import dev.entropy159.entropylib.client.util.WorldToScreen;
import dev.entropy159.entropylib.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public abstract class CapturePoint {
    private static final int ICON_SIZE = 16;

    private BlockPos pos;
    private float captureProgress;
    private boolean contested;
    private @Nullable Character symbol;

    public CapturePoint(BlockPos pos) {
        this(pos, 0, false, null);
    }

    public CapturePoint(BlockPos pos, float captureProgress, boolean contested, @Nullable Character symbol) {
        this.pos = pos;
        this.captureProgress = captureProgress;
        this.contested = contested;
        this.symbol = symbol;
    }

    public CapturePoint(CapturePoint other) {
        this(other.pos, other.captureProgress, other.contested, other.symbol);
    }

    public int getCaptureRadius() {
        return 5;
    }

    public float getCaptureIncrement() {
        return 0.005f;
    }

    public @Nullable Character getSymbol() {
        return symbol;
    }

    public void setSymbol(@Nullable Character symbol) {
        this.symbol = symbol;
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
                contestants.forEach(player -> player.displayClientMessage(Component.translatable("message.arena.capture_point_contested"), true));
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
        return incrementCaptureProgress(level);
    }

    public boolean incrementCaptureProgress(ServerLevel level) {
        captureProgress += getCaptureIncrement() * getPlayersInRadius(level).size();
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

    public void resetCaptureProgress() {
        captureProgress = 0;
    }

    public List<ServerPlayer> getPlayersInRadius(ServerLevel level) {
        return level.players().stream().filter(player -> !player.isSpectator() && isWithinDistance(player.position())).toList();
    }

    public Component getCaptureProgressText() {
        return captureProgress >= 1 ? Component.translatable("message.arena.capture_point_holding").withStyle(ChatFormatting.GREEN) : Component.translatable("message.arena.capture_point_progress", (int) (captureProgress * 100)).withStyle(ChatFormatting.YELLOW);
    }

    public void encodeData(ByteBuf buffer) {
        BlockPos.STREAM_CODEC.encode(buffer, pos);
        ByteBufCodecs.FLOAT.encode(buffer, captureProgress);
        ByteBufCodecs.BOOL.encode(buffer, contested);
        if (symbol != null) {
            buffer.writeBoolean(true);
            buffer.writeChar(symbol);
        } else {
            buffer.writeBoolean(false);
        }
    }

    public void decode(ByteBuf buffer) {
        pos = BlockPos.STREAM_CODEC.decode(buffer);
        captureProgress = ByteBufCodecs.FLOAT.decode(buffer);
        contested = ByteBufCodecs.BOOL.decode(buffer);
        if (buffer.readBoolean()) {
            symbol = buffer.readChar();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int index, int total) {
        int padding = 4;
        int leftPadding = 4;

        renderInWorld(graphics);
        int centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;
        int totalPadding = padding * (total - 1);
        int totalHeight = (ICON_SIZE * total) + totalPadding;
        int topY = centerY - totalHeight / 2;

        int x = leftPadding + ICON_SIZE / 2;
        int y = topY + (index * (ICON_SIZE + padding));
        renderInGUI(graphics, x, y, 255);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderInWorld(GuiGraphics graphics) {
        var pos = WorldToScreen.worldToScreen(getPos().getCenter());
        if (WorldToScreen.inFrontOfCamera(pos)) {
            renderInGUI(graphics, (int) pos.x, (int) pos.y, (int) (RenderingUtils.centerTransparency(pos.x, pos.y) * 255));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderInGUI(GuiGraphics graphics, int x, int y, int alpha) {
        RenderingUtils.renderImageWithDefaultPath(graphics, getIcon(), x, y, ICON_SIZE, Utils.multiplyAlpha(getColor(), alpha / 255f));
        if (symbol != null) {
            var font = Minecraft.getInstance().font;
            var text = String.valueOf(getSymbol());
            var color = new Color(getColor());
            var inverted = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), alpha);
            graphics.drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, inverted.getRGB(), false);
        }
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
    public abstract int getColor();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CapturePoint point && pos.equals(point.pos);
    }
}
