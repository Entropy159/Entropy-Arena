package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.core.gamemodes.Disguise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    @Unique
    private BlockRenderDispatcher blockDispatcher;

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityRendererProvider.Context context, boolean useSlimModel, CallbackInfo ci) {
        blockDispatcher = context.getBlockRenderDispatcher();
    }

    @Redirect(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    public void render(LivingEntityRenderer instance, LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        AbstractClientPlayer player = (AbstractClientPlayer) entity;
        if (ClientData.currentGamemode instanceof Disguise gamemode) {
            BlockState disguise = gamemode.getDisguise(player);
            if (disguise != null && disguise.getRenderShape() == RenderShape.MODEL) {
                shadowStrength = 0;
                Level level = player.level();

                poseStack.pushPose();
                BlockPos blockPos = player.blockPosition();
                poseStack.translate(-0.5, 0.0, -0.5);
                RenderType renderType = blockDispatcher.getBlockModel(disguise).getRenderTypes(disguise, level.random, ModelData.EMPTY).asList().getFirst();
                blockDispatcher.getModelRenderer().tesselateBlock(level, blockDispatcher.getBlockModel(disguise), disguise, blockPos, poseStack, buffer.getBuffer(renderType), false, level.random, disguise.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
                poseStack.popPose();

                return;
            }
        }
        shadowStrength = 1;
        super.render(player, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @SuppressWarnings({"NullableProblems", "DataFlowIssue"})
    @Shadow
    public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return null;
    }
}
