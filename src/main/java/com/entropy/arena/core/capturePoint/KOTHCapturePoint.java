package com.entropy.arena.core.capturePoint;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.capturePoint.CapturePoint;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.core.gamemodes.KingOfTheHill;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class KOTHCapturePoint extends CapturePoint {
    public static final StreamCodec<ByteBuf, KOTHCapturePoint> STREAM_CODEC = StreamCodec.of((buffer, point) -> point.encodeData(buffer), KOTHCapturePoint::decodeData);

    private UUID king;

    public KOTHCapturePoint(BlockPos pos) {
        super(pos);
    }

    public @Nullable UUID getKing() {
        return king;
    }

    public boolean setKing(UUID newKing) {
        if (king != newKing) {
            king = newKing;
            return true;
        }
        return false;
    }

    @Override
    public int getCaptureRadius() {
        return 7;
    }

    @Override
    public float getCaptureIncrement() {
        return super.getCaptureIncrement() * 2;
    }

    @Override
    public void onLevelTick(ServerLevel level) {
        super.onLevelTick(level);
        List<ServerPlayer> contestants = getPlayersInRadius(level);
        level.players().forEach(player -> player.setGlowingTag(contestants.contains(player)));
        if (contestants.isEmpty()) {
            resetCaptureProgress();
            Player oldKing = getKing() == null ? null : level.getPlayerByUUID(getKing());
            if (setKing(null) && oldKing != null) {
                Notification.toAll(Component.translatable("arena.message.koth.hill_lost", oldKing.getDisplayName()).withStyle(ChatFormatting.RED));
            }
        } else if (contestants.size() == 1) {
            if (tryIncrementCapture(level)) {
                if (setKing(contestants.getFirst().getUUID())) {
                    Notification.toAll(Component.translatable("arena.message.koth.new_king", contestants.getFirst().getDisplayName()).withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }

    @Override
    public int getColor(DeltaTracker tracker) {
        boolean inPoint = isLocalPlayerInPoint();
        boolean hasKing = getKing() != null;
        boolean isKing = hasKing && Minecraft.getInstance().isLocalPlayer(getKing());
        if (isKing) {
            return KingOfTheHill.KING_COLOR;
        }
        float alpha = isBeingTaken() || isContested() || inPoint ? ArenaRenderingUtils.sineFromZeroToOne(6, tracker) : 0;
        return ArenaUtils.lerpColors(0xFFFFFFFF, hasKing ? 0xFFFF0000 : KingOfTheHill.KING_COLOR, alpha);
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        ByteBufCodecs.BOOL.encode(buffer, getKing() != null);
        if (getKing() != null) UUIDUtil.STREAM_CODEC.encode(buffer, getKing());
    }

    public static KOTHCapturePoint decodeData(ByteBuf buffer) {
        KOTHCapturePoint point = new KOTHCapturePoint(null);
        point.decode(buffer);
        if (ByteBufCodecs.BOOL.decode(buffer)) {
            point.setKing(UUIDUtil.STREAM_CODEC.decode(buffer));
        }
        return point;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof KOTHCapturePoint other && other.getKing() == getKing();
    }
}
