package com.entropy.arena.core.gamemodes;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.FFAGamemode;
import com.entropy.arena.api.gamemode.HasCapturePoints;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.CapturePointBlock;
import com.entropy.arena.core.capturePoint.KOTHCapturePoint;
import com.entropy.arena.core.map.ArenaMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KingOfTheHill extends FFAGamemode implements HasCapturePoints<KOTHCapturePoint> {
    private static final StreamCodec<ByteBuf, KOTHCapturePoint> CAPTURE_POINT_CODEC = KOTHCapturePoint.STREAM_CODEC;
    public static final int KING_COLOR = 0xFFFFD700;
    private static final int SCORE_DELAY_TICKS = 100;

    private KOTHCapturePoint capturePoint;

    public KingOfTheHill() {
        super(EntropyArena.id("king_of_the_hill"), "King of the Hill");
    }

    @Override
    public void generateLang() {
        super.generateLang();
        EntropyArena.REGISTRATE.addRawLang("arena.message.koth.new_king", "%s has taken the hill");
        EntropyArena.REGISTRATE.addRawLang("arena.message.koth.hill_lost", "%s has lost the hill");
    }

    @Override
    public void onLevelTick(ArenaData data) {
        super.onLevelTick(data);
        if (capturePoint == null) return;
        capturePoint.onLevelTick(data.getLevel());
        if (data.getLevel().getGameTime() % SCORE_DELAY_TICKS == 0) {
            if (capturePoint.getKing() != null && data.getLevel().getPlayerByUUID(capturePoint.getKing()) instanceof ServerPlayer king) {
                incrementScore(king);
            }
        }
        sendToAll();
    }

    @Override
    public @Nullable Component validateMap(ServerLevel level, ArenaMap arenaMap) {
        Component failureMessage = super.validateMap(level, arenaMap);
        if (failureMessage != null) return failureMessage;
        int capturePoints = calculateCapturePoints(arenaMap, level, KOTHCapturePoint::new).size();
        if (capturePoints == 0) return Component.translatable("arena.error.no_capture_points");
        if (capturePoints > 1) return Component.translatable("arena.error.too_many_capture_points", 1);
        return null;
    }

    @Override
    public int modifyEntityColor(Entity entity, int color) {
        if (getCapturePoint() != null && entity.getUUID().equals(getCapturePoint().getKing())) {
            return KING_COLOR;
        }
        return super.modifyEntityColor(entity, color);
    }

    @Override
    public void onMatchStart(ArenaData data) {
        super.onMatchStart(data);
        capturePoint = calculateCapturePoints(data.getCurrentMap(), data.getLevel(), KOTHCapturePoint::new).getFirst();
//        removeCapturePointBlocks(data.getLevel());
        sendToAll();
    }

    private void removeCapturePointBlocks(ServerLevel level) {
        level.setBlock(capturePoint.getPos(), level.getBlockState(capturePoint.getPos()).setValue(CapturePointBlock.VISIBLE, false), Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
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
