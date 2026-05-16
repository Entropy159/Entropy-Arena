package com.entropy.arena.client;

import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public record PingIcon(Vector3f pos, int color, long timestamp) {
    public static final int DURATION = 60;

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, DeltaTracker tracker) {
        if (Minecraft.getInstance().level == null || expired()) return;
        float alpha = 1 - (Minecraft.getInstance().level.getGameTime() + tracker.getGameTimeDeltaPartialTick(true) - timestamp) / (float) DURATION;
        ArenaRenderingUtils.renderImageAtWorldPos(graphics, EntropyArena.id("ping"), new Vec3(pos.x, pos.y, pos.z), 8, (Math.round(alpha * 255) << 24) | (color & 0xFFFFFF), false);
    }

    public boolean expired() {
        return Minecraft.getInstance().level == null || Minecraft.getInstance().level.getGameTime() - DURATION >= timestamp;
    }
}
