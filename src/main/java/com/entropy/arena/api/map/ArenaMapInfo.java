package com.entropy.arena.api.map;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.network.toServer.MapVotePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public record ArenaMapInfo(String name, MapScreenshot screenshot, ResourceLocation gamemode, Vec3i size) {
    public static final StreamCodec<ByteBuf, ArenaMapInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ArenaMapInfo::name, MapScreenshot.STREAM_CODEC, ArenaMapInfo::screenshot, ResourceLocation.STREAM_CODEC, ArenaMapInfo::gamemode, ArenaUtils.VEC3I_STREAM_CODEC, ArenaMapInfo::size, ArenaMapInfo::new);
    public static final int TEXT_PADDING = 2;
    public static final int FRAME_PADDING = 2;
    public static final Color NAME_COLOR = Color.CYAN;
    public static final Color GAMEMODE_COLOR = Color.YELLOW;
    public static final Color SIZE_COLOR = Color.RED;
    public static final Color FRAME_COLOR = Color.LIGHT_GRAY;
    public static final Color FRAME_SELECTED_COLOR = Color.WHITE;

    @OnlyIn(Dist.CLIENT)
    public Button getWidget(int x, int y, int width) {
        int lineCount = 3;
        int lineHeight = Minecraft.getInstance().font.lineHeight + TEXT_PADDING;
        int imageHeight = (int) (width * screenshot.aspectRatio);
        int totalHeight = imageHeight + lineHeight * lineCount + TEXT_PADDING;
        return new MapButton(x, y, width, totalHeight, this);
    }

    @OnlyIn(Dist.CLIENT)
    private static class MapButton extends Button {
        private final ArenaMapInfo mapInfo;

        public MapButton(int x, int y, int width, int height, ArenaMapInfo info) {
            super(x, y, width, height, CommonComponents.EMPTY, button -> PacketDistributor.sendToServer(new MapVotePacket(info.name)), DEFAULT_NARRATION);
            mapInfo = info;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Minecraft client = Minecraft.getInstance();
            Font font = client.font;
            ArenaGamemode mode = GamemodeRegistry.getGamemode(mapInfo.gamemode);
            int lineHeight = client.font.lineHeight + TEXT_PADDING;
            int imageHeight = (int) (width * mapInfo.screenshot.aspectRatio);
            int totalHeight = imageHeight + lineHeight * 3 + TEXT_PADDING;
            int centerX = getX() + width / 2;
            int textStartY = getY() + imageHeight + TEXT_PADDING;
            ScreenLocation location = new ScreenLocation(mapInfo.name, getX(), getY(), width, totalHeight);
            mapInfo.screenshot.render(graphics, getX() + FRAME_PADDING, getY() + FRAME_PADDING, width - FRAME_PADDING * 2);
            graphics.drawCenteredString(font, mapInfo.name, centerX, textStartY, NAME_COLOR.getRGB());
            if (mode != null)
                graphics.drawCenteredString(font, mode.getName(), centerX, textStartY + lineHeight, GAMEMODE_COLOR.getRGB());
            graphics.drawCenteredString(font, "%sx%sx%s".formatted(mapInfo.size.getX(), mapInfo.size.getY(), mapInfo.size.getZ()), centerX, textStartY + lineHeight * 2, SIZE_COLOR.getRGB());
            graphics.renderOutline(getX(), getY(), width, totalHeight, location.isWithinBounds(mouseX, mouseY) ? FRAME_SELECTED_COLOR.getRGB() : FRAME_COLOR.getRGB());
        }
    }

    public record ScreenLocation(String name, double x, double y, double width, double height) {
        public boolean tryClick(double mouseX, double mouseY) {
            if (isWithinBounds(mouseX, mouseY)) {
                PacketDistributor.sendToServer(new MapVotePacket(name));
                return true;
            }
            return false;
        }

        private boolean isWithinBounds(double mouseX, double mouseY) {
            return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
        }
    }
}
