package com.entropy.arena.api.client;

import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.config.ClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
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

    public static float sineFromZeroToOne(float scale) {
        if (client.level == null) return 0;
        float time = Util.getMillis() / 50f;
        float scaledTime = time / scale;
        return (float) (Math.sin(scaledTime) + 1) / 2;
    }

    public static void renderStickyImageAtWorldPos(GuiGraphics graphics, ResourceLocation location, Vec3 worldPos, int size, int color) {
        Vec3 screenPos = worldToScreen(worldPos);
        int x = (int) screenPos.x;
        int y = (int) screenPos.y;
        int padding = size / 2;
        int maxX = client.getWindow().getGuiScaledWidth() - padding;
        int maxY = client.getWindow().getGuiScaledHeight() - padding;
        if (y < padding || y > maxY || x < padding || x > maxX || !WorldToScreen.inFrontOfCamera(screenPos)) {
            if (!WorldToScreen.inFrontOfCamera(screenPos)) {
                x = -x;
                y = -y;
            }
            Vec2 newPos = WorldToScreen.calculateAngleRectIntersection((float) Math.atan2(y, x), new Vec2(padding, padding), new Vec2(maxX, maxY));
            x = (int) newPos.x;
            y = (int) newPos.y;
            if (y <= padding) {
                location = EntropyArena.id("up");
            }
            if (y >= maxY) {
                location = EntropyArena.id("down");
            }
            if (x <= padding) {
                location = EntropyArena.id("left");
            }
            if (x >= maxX) {
                location = EntropyArena.id("right");
            }
        }
        renderImageWithDefaultPath(graphics, location, x, y, size, color);
    }

    public static void renderImageAtWorldPos(GuiGraphics graphics, ResourceLocation location, Vec3 worldPos, int size, int color) {
        Vec3 screenPos = worldToScreen(worldPos);
        if (WorldToScreen.inFrontOfCamera(screenPos)) {
            renderImageWithDefaultPath(graphics, location, (int) screenPos.x, (int) screenPos.y, size, color);
        }
    }

    public static void renderImageAtWorldPosCenterAlpha(GuiGraphics graphics, ResourceLocation location, Vec3 worldPos, int size, int color) {
        Vec3 screenPos = worldToScreen(worldPos);
        float alpha = centerTransparency(screenPos.x, screenPos.y) * new Color(color).getAlpha() / 255f;
        if (WorldToScreen.inFrontOfCamera(screenPos)) {
            renderImageWithDefaultPath(graphics, location, (int) screenPos.x, (int) screenPos.y, size, (Math.round(alpha * 255) << 24) | (color & 0xFFFFFF));
        }
    }

    public static float centerTransparency(double x, double y) {
        Vec3 pos = new Vec3(x / client.getWindow().getGuiScaledWidth(), y / client.getWindow().getGuiScaledHeight(), 0);
        Vec3 center = new Vec3(0.5, 0.5, 0);
        double centerRadius = ClientConfig.SCREEN_CENTER_NO_ICONS.getAsDouble();
        double distance = pos.distanceTo(center);
        float value = (float) (((distance * 2) - centerRadius) / centerRadius);
        return Math.clamp(value, 0.5f, 1);
    }

    public static void renderImageWithDefaultPath(GuiGraphics graphics, ResourceLocation location, int x, int y, int size, int color) {
        renderSquareImage(graphics, ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "textures/gui/" + location.getPath() + ".png"), x, y, size, color);
    }

    public static void renderSquareImage(GuiGraphics graphics, ResourceLocation location, int x, int y, int size, int color) {
        renderImage(graphics, location, x, y, size, size, color);
    }

    public static void renderImage(GuiGraphics graphics, ResourceLocation location, int x, int y, int width, int height, int color) {
        Color c = new Color(color, true);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        client.getTextureManager().bindForSetup(location);
        int x1 = x - width / 2;
        int y1 = y - height / 2;
        graphics.innerBlit(location, x1, x1 + width, y1, y1 + width, 0, 0, 1, 0, 1, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }
}
