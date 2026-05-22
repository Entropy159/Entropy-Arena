package com.entropy.arena.client;

import com.entropy.arena.api.util.ArenaUtils;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public record PingIcon(Vector3f pos, int color, long timestamp) {
    public static final int DURATION = 3000;

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics) {
        if (Minecraft.getInstance().level == null || expired()) return;
        float alpha = 1 - (Util.getMillis() - timestamp) / (float) DURATION;
        ArenaRenderingUtils.renderImageAtWorldPosCenterFade(graphics, EntropyArena.id("ping"), new Vec3(pos.x, pos.y, pos.z), 8, ArenaUtils.colorWithAlpha(color, alpha));
    }

    public boolean expired() {
        return Minecraft.getInstance().level == null || Util.getMillis() - DURATION >= timestamp;
    }
}
