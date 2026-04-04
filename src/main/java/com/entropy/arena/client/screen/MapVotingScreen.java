package com.entropy.arena.client.screen;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.map.ArenaMapInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MapVotingScreen extends Screen {
    private static final ArrayList<ArenaMapInfo.ScreenLocation> MAP_LOCATIONS = new ArrayList<>();

    public MapVotingScreen() {
        super(Component.translatable("arena.screen.map_voting"));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

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
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientData.votableMaps.forEach(info -> info.screenshot().clear());
    }
}
