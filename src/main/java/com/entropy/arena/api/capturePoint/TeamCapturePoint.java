package com.entropy.arena.api.capturePoint;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamCapturePoint extends CapturePoint {
    public static final StreamCodec<ByteBuf, TeamCapturePoint> STREAM_CODEC = StreamCodec.of((buffer, point) -> point.encodeData(buffer), TeamCapturePoint::decodeData);

    private ArenaTeam team = ArenaTeam.NONE;
    private ArenaTeam takingTeam = ArenaTeam.NONE;

    public TeamCapturePoint(BlockPos pos) {
        super(pos);
    }

    public TeamCapturePoint(CapturePoint point, ArenaTeam team) {
        super(point);
        this.team = team;
    }

    public ArenaTeam getTeam() {
        return team;
    }

    public boolean setTeam(@NotNull ArenaTeam newTeam) {
        if (team != newTeam) {
            team = newTeam;
            return true;
        }
        return false;
    }

    @Override
    public void onLevelTick(ServerLevel level) {
        super.onLevelTick(level);
        List<ArenaTeam> contestants = getPlayersInRadius(level).stream().map(player -> ArenaTeam.fromTeam(player.getTeam())).distinct().toList();
        if (contestants.isEmpty()) {
            resetCaptureProgress();
        } else if (contestants.size() == 1) {
            ArenaTeam team = contestants.getFirst();
            takingTeam = team;
            if (tryIncrementCapture(level)) {
                if (setTeam(team)) {
                    Notification.toAll(Component.translatable("arena.message.team_capture_point_taken", team.getColoredName()).withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }

    @Override
    public boolean playersAreCompatible(ServerPlayer one, ServerPlayer two) {
        return one.getTeam() != null && one.getTeam().equals(two.getTeam());
    }

    public void encodeData(ByteBuf buffer) {
        super.encodeData(buffer);
        ArenaTeam.STREAM_CODEC.encode(buffer, team);
        ArenaTeam.STREAM_CODEC.encode(buffer, takingTeam);
    }

    public static TeamCapturePoint decodeData(ByteBuf buffer) {
        TeamCapturePoint point = new TeamCapturePoint(null);
        point.decode(buffer);
        point.team = ArenaTeam.STREAM_CODEC.decode(buffer);
        point.takingTeam = ArenaTeam.STREAM_CODEC.decode(buffer);
        return point;
    }

    @Override
    public int getColor(DeltaTracker tracker) {
        float alpha = ArenaRenderingUtils.sineFromZeroToOne(6, tracker);
        if (isBeingTaken()) {
            return ArenaUtils.lerpColors(getTeam().getColor(), takingTeam.getColor(), alpha);
        }
        return getTeam().getColor();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TeamCapturePoint point && super.equals(obj) && team == point.team;
    }
}
