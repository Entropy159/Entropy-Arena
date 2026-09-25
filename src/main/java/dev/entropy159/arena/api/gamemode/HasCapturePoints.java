package dev.entropy159.arena.api.gamemode;

import dev.entropy159.arena.api.capturePoint.CapturePoint;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.blocks.CapturePointBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface HasCapturePoints<T extends CapturePoint> {
    default ArrayList<T> calculateCapturePoints(ServerLevel level, ArenaMap currentMap, CapturePointGetter<T> getter) {
        var list = new ArrayList<>(currentMap.getBlockPropertyMap(level, CapturePointBlock.VISIBLE).values().stream().reduce(new ArrayList<>(), (l, obj) -> {
            l.addAll(obj);
            return l;
        }));
        list.sort(Comparator.naturalOrder());
        return new ArrayList<>(list.stream().map(pos -> getter.convert(level, list.indexOf(pos), list.size(), pos)).toList());
    }

    List<T> getCapturePoints();

    @FunctionalInterface
    interface CapturePointGetter<T extends CapturePoint> {
        T convert(ServerLevel level, int current, int total, BlockPos pos);
    }
}
