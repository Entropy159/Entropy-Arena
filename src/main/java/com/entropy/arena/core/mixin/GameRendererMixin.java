package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.WorldToScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    private void renderer_postWorldRender(LevelRenderer instance, DeltaTracker tracker, boolean i, Camera camera, GameRenderer renderer, LightTexture lightTexture, Matrix4f positionMatrix, Matrix4f projectionMatrix, Operation<Void> original) {
        original.call(instance, tracker, i, camera, renderer, lightTexture, positionMatrix, projectionMatrix);

        PoseStack matrix = new PoseStack();
        matrix.mulPose(positionMatrix);

        WorldToScreen.lastProjMat.set(projectionMatrix);
        WorldToScreen.lastModMat.set(RenderSystem.getModelViewMatrix());
        WorldToScreen.lastWorldSpaceMatrix.set(matrix.last().pose());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, WorldToScreen.lastViewport);

        // restore state like the original world rendering code did
        GlStateManager._depthMask(true);
        GlStateManager._disableBlend();
    }
}
