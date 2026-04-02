package com.entropy.arena.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.awt.*;

public enum ScreenAnchorPoint {
    TOP_LEFT {
        @Override
        public void renderText(GuiGraphics graphics, Component text, int line, float alpha) {
            Minecraft client = Minecraft.getInstance();
            int padding = ArenaRenderingUtils.getTextPadding();
            graphics.drawString(client.font, text, padding, ArenaRenderingUtils.textLine(line), new Color(1, 1, 1, alpha).getRGB());
        }
    },
    TOP_CENTER {
        @Override
        public void renderText(GuiGraphics graphics, Component text, int line, float alpha) {
            Minecraft client = Minecraft.getInstance();
            graphics.drawCenteredString(client.font, text, client.getWindow().getGuiScaledWidth() / 2, ArenaRenderingUtils.textLine(line), new Color(1, 1, 1, alpha).getRGB());
        }
    },
    TOP_RIGHT {
        @Override
        public void renderText(GuiGraphics graphics, Component text, int line, float alpha) {
            Minecraft client = Minecraft.getInstance();
            graphics.drawString(client.font, text, ArenaRenderingUtils.alignRight(text), ArenaRenderingUtils.textLine(line), new Color(1, 1, 1, alpha).getRGB());
        }
    },
    BOTTOM_LEFT {
        @Override
        public void renderText(GuiGraphics graphics, Component text, int line, float alpha) {
            Minecraft client = Minecraft.getInstance();
            int padding = ArenaRenderingUtils.getTextPadding();
            graphics.drawString(client.font, text, padding, ArenaRenderingUtils.alignBottom(line), new Color(1, 1, 1, alpha).getRGB());
        }
    },
    BOTTOM_RIGHT {
        @Override
        public void renderText(GuiGraphics graphics, Component text, int line, float alpha) {
            Minecraft client = Minecraft.getInstance();
            graphics.drawString(client.font, text, ArenaRenderingUtils.alignRight(text), ArenaRenderingUtils.alignBottom(line), new Color(1, 1, 1, alpha).getRGB());
        }
    };

    public abstract void renderText(GuiGraphics graphics, Component text, int line, float alpha);
}
