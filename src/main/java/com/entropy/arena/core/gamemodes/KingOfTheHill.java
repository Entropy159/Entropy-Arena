package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.api.gamemode.HasCapturePoints;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.capturePoint.KOTHCapturePoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KingOfTheHill extends FFAGamemode implements HasCapturePoints<KOTHCapturePoint> {
    protected static final StreamCodec<ByteBuf, KOTHCapturePoint> CAPTURE_POINT_CODEC = KOTHCapturePoint.STREAM_CODEC;
    public static final int KING_COLOR = 0xFFFFD700;
    protected static final int SCORE_DELAY_TICKS = 100;

    protected KOTHCapturePoint capturePoint;

    public KingOfTheHill(ResourceLocation id) {
        super(id);
    }

    @Override
    public void onLevelTick(ServerLevel level) {
        super.onLevelTick(level);
        if (capturePoint == null) return;
        capturePoint.onLevelTick(level);
        if (level.getGameTime() % SCORE_DELAY_TICKS == 0) {
            if (capturePoint.getKing() != null && level.getPlayerByUUID(capturePoint.getKing()) instanceof ServerPlayer king) {
                incrementScore(king);
            }
        }
        sendToAll();
    }

    @Override
    public void onEntityTick(ServerLevel level, Entity entity) {
        super.onEntityTick(level, entity);
        if (entity instanceof ServerPlayer player) {
            (capturePoint.getKing() == player.getUUID() ? ArenaTeam.YELLOW : ArenaTeam.NONE).setThisTeam(player);
        }
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        int capturePoints = calculateCapturePoints(level, arenaMap, KOTHCapturePoint::new).size();
        if (capturePoints == 0) return Component.translatable("arena.error.no_capture_points");
        if (capturePoints > 1) return Component.translatable("arena.error.too_many_capture_points", 1);
        return null;
    }

    @Override
    public void onMatchStart(ServerLevel level) {
        super.onMatchStart(level);
        capturePoint = calculateCapturePoints(level, ArenaData.get(level).currentMap, KOTHCapturePoint::new).getFirst();
        sendToAll();
    }

    public KOTHCapturePoint getCapturePoint() {
        return capturePoint;
    }

    public List<KOTHCapturePoint> getCapturePoints() {
        return List.of(getCapturePoint());
    }

    @Override
    public List<Component> getScoreText(ServerLevel level) {
        ServerPlayer king = (getCapturePoint() == null || getCapturePoint().getKing() == null) ? null : (ServerPlayer) level.getPlayerByUUID(getCapturePoint().getKing());
        return super.getScoreText(level).stream().map(component -> {
            if (king != null && component.getString().startsWith(king.getDisplayName().getString() + ": ")) {
                return component.copy().withColor(KING_COLOR);
            }
            return component;
        }).toList();
    }

    @Override
    public void onClientRender(GuiGraphics graphics, DeltaTracker tracker) {
        super.onClientRender(graphics, tracker);
        capturePoint.render(graphics, tracker);
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        ByteBufCodecs.BOOL.encode(buffer, getCapturePoint() != null);
        if (getCapturePoint() != null) CAPTURE_POINT_CODEC.encode(buffer, getCapturePoint());
    }

    public void decodeData(ByteBuf buffer) {
        super.decodeData(buffer);
        if (ByteBufCodecs.BOOL.decode(buffer)) {
            capturePoint = CAPTURE_POINT_CODEC.decode(buffer);
        }
    }
}
