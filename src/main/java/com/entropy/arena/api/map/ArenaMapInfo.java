package com.entropy.arena.api.map;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.network.toServer.MapVotePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

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
    public ScreenLocation render(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        ArenaGamemode mode = GamemodeRegistry.getGamemode(gamemode);
        int lineHeight = client.font.lineHeight + TEXT_PADDING;
        int imageHeight = (int) (width * screenshot.aspectRatio);
        int totalHeight = imageHeight + lineHeight * 3 + TEXT_PADDING;
        int centerX = x + width / 2;
        int textStartY = y + imageHeight + TEXT_PADDING;
        ScreenLocation location = new ScreenLocation(name, x, y, width, totalHeight);
        screenshot.render(graphics, x + FRAME_PADDING, y + FRAME_PADDING, width - FRAME_PADDING * 2);
        graphics.drawCenteredString(font, name, centerX, textStartY, NAME_COLOR.getRGB());
        if (mode != null)
            graphics.drawCenteredString(font, mode.getName(), centerX, textStartY + lineHeight, GAMEMODE_COLOR.getRGB());
        graphics.drawCenteredString(font, "%sx%sx%s".formatted(size.getX(), size.getY(), size.getZ()), centerX, textStartY + lineHeight * 2, SIZE_COLOR.getRGB());
        graphics.renderOutline(x, y, width, totalHeight, location.isWithinBounds(mouseX, mouseY) ? FRAME_SELECTED_COLOR.getRGB() : FRAME_COLOR.getRGB());
        return location;
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
