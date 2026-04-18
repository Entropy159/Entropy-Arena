package com.entropy.arena.client.screen;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.map.ArenaMapInfo;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.network.toServer.TypeVotePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class VotingScreen extends Screen {
    public VotingScreen() {
        super(Component.translatable("arena.screen.voting"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 75;
        int buttonHeight = font.lineHeight + 8;
        int buttonPadding = 5;
        addRenderableWidget(new Button.Builder(Component.translatable("arena.hud.timed"), button -> {
            PacketDistributor.sendToServer(new TypeVotePacket(true));
        }).size(buttonWidth, buttonHeight).pos(width / 2 - buttonPadding - buttonWidth, height * 2 / 3).build());
        addRenderableWidget(new Button.Builder(Component.translatable("arena.hud.score"), button -> {
            PacketDistributor.sendToServer(new TypeVotePacket(false));
        }).size(buttonWidth, buttonHeight).pos(width / 2 + buttonPadding, height * 2 / 3).build());

        int totalMaps = ClientData.votableMaps.size();
        int currentMap = 1;
        for (ArenaMapInfo mapInfo : ClientData.votableMaps) {
            addRenderableWidget(mapInfo.getWidget(getMapX(currentMap, totalMaps), height / 4, getMapWidth()));
            currentMap++;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (ClientData.votableMaps.isEmpty()) {
            onClose();
        }
    }

    private int getMapX(int current, int total) {
        int mapWidth = getMapWidth();
        return (width / (total + 1) * current) - mapWidth / 2;
    }

    private int getMapWidth() {
        return width / (ArenaLogic.MAPS_FOR_VOTING + 1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientData.votableMaps.forEach(info -> info.screenshot().clear());
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {

    }
}
