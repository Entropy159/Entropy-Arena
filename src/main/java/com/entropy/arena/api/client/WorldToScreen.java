package com.entropy.arena.api.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Credits to the Ping Wheel mod for this implementation!
 */
public class WorldToScreen {
    public static Matrix4f modelViewMatrix = new Matrix4f();
    public static Matrix4f projectionMatrix = new Matrix4f();

    @OnlyIn(Dist.CLIENT)
    public static Vec3 worldToScreen(Vec3 worldPos) {
        Window window = Minecraft.getInstance().getWindow();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector4f worldPosRel = new Vector4f(camera.getPosition().reverse().add(worldPos).toVector3f(), 1.0F);
        worldPosRel.mul(modelViewMatrix);
        worldPosRel.mul(projectionMatrix);

        float depth = worldPosRel.w;

        if (depth != 0) {
            worldPosRel.div(depth);
        }

        return new Vec3(
                window.getGuiScaledWidth() * (0.5F + worldPosRel.x * 0.5F),
                window.getGuiScaledHeight() * (0.5F - worldPosRel.y * 0.5F),
                depth
        );
    }

    public static boolean inFrontOfCamera(Vec3 screenPos) {
        return screenPos.z > 0;
    }
}
