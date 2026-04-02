package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.capturePoint.TeamCapturePoint;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.HasCapturePoints;
import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class Domination extends TeamGamemode implements HasCapturePoints<TeamCapturePoint> {
    private static final StreamCodec<ByteBuf, List<TeamCapturePoint>> CAPTURE_POINTS_CODEC = TeamCapturePoint.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final int SCORING_DELAY_TICKS = 100;

    private List<TeamCapturePoint> capturePoints = new ArrayList<>();

    public Domination() {
        super(EntropyArena.id("domination"), "Domination");
    }

    @Override
    public void onMatchStart(ArenaData data) {
        super.onMatchStart(data);
        capturePoints = calculateCapturePoints(data.getCurrentMap(), data.getLevel(), TeamCapturePoint::new);
        sendToAll();
    }

    @Override
    public void onLevelTick(ArenaData data) {
        super.onLevelTick(data);
        capturePoints.forEach(point -> point.onLevelTick(data.getLevel()));
        if (data.getLevel().getGameTime() % SCORING_DELAY_TICKS == 0) {
            capturePoints.forEach(point -> {
                if (point.getTeam() != ArenaTeam.NONE) {
                    incrementScore(point.getTeam());
                }
            });
        }
        sendToAll();
    }

    public List<TeamCapturePoint> getCapturePoints() {
        return capturePoints;
    }

    @Override
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
        super.onClientRender(graphics, tracker);
        capturePoints.forEach(point -> point.render(graphics, tracker));
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        CAPTURE_POINTS_CODEC.encode(buffer, getCapturePoints());
    }

    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        capturePoints = CAPTURE_POINTS_CODEC.decode(buffer);
    }
}
