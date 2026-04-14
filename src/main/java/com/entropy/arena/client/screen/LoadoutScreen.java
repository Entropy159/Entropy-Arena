package com.entropy.arena.client.screen;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.core.network.toServer.LoadoutSelectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class LoadoutScreen extends Screen {
    public LoadoutScreen() {
        super(Component.translatable("arena.screen.loadout"));
    }

    @Override
    protected void init() {
        super.init();
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        int totalLoadouts = ClientData.loadouts.size();
        int buttonHeight = font.lineHeight + 8;
        int buttonPadding = 2;
        int buttonWidth = width / 4;
        int buttonX = (width - buttonWidth) / 2;
        int startY = (height - (buttonHeight + buttonPadding) * totalLoadouts) / 2;
        int currentLine = 0;
        for (String loadout : ClientData.loadouts) {
            addRenderableWidget(new Button.Builder(Component.literal(loadout), button -> {
                PacketDistributor.sendToServer(new LoadoutSelectPacket(loadout));
                onClose();
            }).size(width / 4, buttonHeight).pos(buttonX, startY + (buttonHeight + buttonPadding) * currentLine).build());
            currentLine++;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (ClientData.loadouts.isEmpty()) {
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {

    }
}
