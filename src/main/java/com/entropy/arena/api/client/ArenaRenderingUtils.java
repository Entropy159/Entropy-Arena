package com.entropy.arena.api.client;

import com.entropy.arena.core.config.ClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.HashMap;

import static com.entropy.arena.api.client.WorldToScreen.worldToScreen;

@OnlyIn(Dist.CLIENT)
public class ArenaRenderingUtils {
    private static final Minecraft client = Minecraft.getInstance();
    private static final int hudTextPadding = 2;
    private static final HashMap<ScreenAnchorPoint, Integer> currentLineMap = new HashMap<>();

    public static void renderText(GuiGraphics graphics, Component text, ScreenAnchorPoint anchor) {
        renderTextWithAlpha(graphics, text, anchor, 1);
    }

    public static void renderTextWithAlpha(GuiGraphics graphics, Component text, ScreenAnchorPoint anchor, float alpha) {
        int currentLine = currentLineMap.getOrDefault(anchor, 0);
        anchor.renderText(graphics, text, currentLine, alpha);
        currentLineMap.put(anchor, currentLine + 1);
    }

    public static void onRenderStart() {
        currentLineMap.clear();
    }

    public static int getTextPadding() {
        return hudTextPadding;
    }

    public static int textLine(int line) {
        return (client.font.lineHeight + getTextPadding()) * line + getTextPadding();
    }

    public static int alignRight(Component text) {
        return alignRight(client.font.width(text.getVisualOrderText()));
    }

    public static int alignRight(int width) {
        return client.getWindow().getGuiScaledWidth() - (getTextPadding() + width);
    }

    public static int alignBottom(int line) {
        return client.getWindow().getGuiScaledHeight() - client.font.lineHeight - textLine(line);
    }

    public static float sineFromZeroToOne(float scale, DeltaTracker tracker) {
        if (client.level == null) return 0;
        float time = client.level.getGameTime() + tracker.getGameTimeDeltaPartialTick(true);
        float scaledTime = time / scale;
        return (float) (Math.sin(scaledTime) + 1) / 2;
    }

    public static void renderImageAtWorldPos(GuiGraphics graphics, ResourceLocation location, Vec3 worldPos, int size, int color) {
        Vec3 screenPos = worldToScreen(worldPos);
        if (screenPos.z < 1 && isOutsideScreenCenter(screenPos.x, screenPos.y))
            renderImageWithDefaultPath(graphics, location, (int) screenPos.x, (int) screenPos.y, size, color);
    }

    public static boolean isOutsideScreenCenter(double x, double y) {
        Vec3 pos = new Vec3(x, y, 0);
        Vec3 center = new Vec3(client.getWindow().getGuiScaledWidth() / 2d, client.getWindow().getGuiScaledHeight() / 2d, 0);
        double centerRadius = ClientConfig.SCREEN_CENTER_NO_ICONS.getAsDouble();
        double centerRadiusX = centerRadius * client.getWindow().getGuiScaledWidth() / 2;
        double centerRadiusY = centerRadius * client.getWindow().getGuiScaledHeight() / 2;
        return !pos.closerThan(center, centerRadiusX, centerRadiusY);
    }

    public static void renderImageWithDefaultPath(GuiGraphics graphics, ResourceLocation location, int x, int y, int size, int color) {
        renderSquareImage(graphics, ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "textures/gui/" + location.getPath() + ".png"), x, y, size, color);
    }

    public static void renderSquareImage(GuiGraphics graphics, ResourceLocation location, int x, int y, int size, int color) {
        renderImage(graphics, location, x, y, size, size, color);
    }

    public static void renderImage(GuiGraphics graphics, ResourceLocation location, int x, int y, int width, int height, int color) {
        Color c = new Color(color);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        client.getTextureManager().bindForSetup(location);
        int x1 = x - width / 2;
        int y1 = y - height / 2;
        graphics.innerBlit(location, x1, x1 + width, y1, y1 + width, 0, 0, 1, 0, 1, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }
}
