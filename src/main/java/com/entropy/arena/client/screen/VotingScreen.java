package com.entropy.arena.client.screen;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.map.ArenaMapInfo;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.network.toServer.TypeVotePacket;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VotingScreen extends Screen {
    private final ArrayList<ArenaMapInfo.ScreenLocation> MAP_LOCATIONS = new ArrayList<>();
    private final ArrayList<TypeButton> TYPE_BUTTONS = new ArrayList<>();
    private static final int TYPE_VOTE_PADDING = 5;

    public VotingScreen() {
        super(Component.translatable("arena.screen.voting"));
    }

    @Override
    protected void init() {
        super.init();
        TYPE_BUTTONS.clear();
        int center = width / 2;
        int typeY = height * 2 / 3;
        int timedWidth = font.width("Timed");
        int timedX = center - TYPE_VOTE_PADDING - timedWidth;
        int scoreX = center + TYPE_VOTE_PADDING;
        TYPE_BUTTONS.add(new TypeButton(timedX, typeY, "Timed"));
        TYPE_BUTTONS.add(new TypeButton(scoreX, typeY, "Score"));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (ClientData.votableMaps.isEmpty()) {
            onClose();
            return;
        }

        MAP_LOCATIONS.clear();
        int i = 1;
        int total = ClientData.votableMaps.size();
        for (ArenaMapInfo mapInfo : ClientData.votableMaps) {
            int x = getMapX(i, total);
            int y = height / 4;
            int width = getMapWidth();
            MAP_LOCATIONS.add(mapInfo.render(graphics, x, y, width, mouseX, mouseY));
            i++;
        }

        TYPE_BUTTONS.forEach(button -> button.render(graphics, font));
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ArenaMapInfo.ScreenLocation location : MAP_LOCATIONS) {
            if (location.tryClick(mouseX, mouseY)) {
                return true;
            }
        }

        int center = width / 2;
        int typeY = height * 2 / 3;
        int timedWidth = font.width("Timed");
        int scoreWidth = font.width("Score");
        int timedX = center - TYPE_VOTE_PADDING - timedWidth;
        int scoreX = center + TYPE_VOTE_PADDING;

        if (mouseY > typeY && mouseY < typeY + font.lineHeight) {
            if (mouseX > timedX && mouseX < timedX + timedWidth) {
                PacketDistributor.sendToServer(new TypeVotePacket(true));
            } else if (mouseX > scoreX && mouseX < scoreX + scoreWidth) {
                PacketDistributor.sendToServer(new TypeVotePacket(false));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientData.votableMaps.forEach(info -> info.screenshot().clear());
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {

    }

    private record TypeButton(int x, int y, String text) {
        private static final int PADDING = 2;

        int getWidth(Font font) {
            return font.width(text) + PADDING * 2;
        }

        int getHeight(Font font) {
            return font.lineHeight + PADDING * 2;
        }

        void render(GuiGraphics graphics, Font font) {
            graphics.renderOutline(x, y, getWidth(font), getHeight(font), 0xFFFFFFFF);
            graphics.drawCenteredString(font, text, x + getWidth(font) / 2, y + PADDING, 0xFFFFFFFF);
        }

        boolean isWithinBounds(double mouseX, double mouseY, Font font) {
            return mouseX >= x && mouseX <= x + getWidth(font) && mouseY >= y && mouseY <= y + getHeight(font);
        }
    }
}
