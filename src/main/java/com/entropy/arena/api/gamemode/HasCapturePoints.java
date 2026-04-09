package com.entropy.arena.api.gamemode;

import com.entropy.arena.api.capturePoint.CapturePoint;
import com.entropy.arena.core.blocks.CapturePointBlock;
import com.entropy.arena.api.map.ArenaMap;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface HasCapturePoints<T extends CapturePoint> {
    default ArrayList<T> calculateCapturePoints(ArenaMap currentMap, Function<BlockPos, T> getter) {
        return new ArrayList<>(currentMap.getBlockPropertyMap(CapturePointBlock.VISIBLE).values().stream().reduce(new ArrayList<>(), (list, obj) -> {
            list.addAll(obj);
            return list;
        }).stream().map(getter).toList());
    }

    List<T> getCapturePoints();
}
