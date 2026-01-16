package com.spydnel.backpacks.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.blocks.BackpackBlock;
import com.spydnel.backpacks.blocks.BackpackBlockEntity;
import com.spydnel.backpacks.registry.BPLayers;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public class BackpackBlockRenderer implements BlockEntityRenderer<BackpackBlockEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpacks.MODID, "textures/entity/backpack.png");
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Backpacks.MODID, "textures/entity/backpack_overlay.png");
    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(Backpacks.MODID, "textures/entity/backpack_base.png");
    private final ModelPart base;
    private final ModelPart lid;

    public BackpackBlockRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelPart = context.bakeLayer(BPLayers.BACKPACK_BLOCK);
        this.base = modelPart.getChild("base");
        this.lid = base.getChild("lid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -11.0F, -4.0F, 10.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition lid = base.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 19).addBox(-5.5F, -2.0F, -0.5F, 11.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(-9, 33).addBox(-5.5F, 1.0F, -0.5F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void render(BackpackBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        boolean isFloating = blockEntity.getBlockState().getValue(BackpackBlock.FLOATING);
        float dir = ((Direction)blockEntity.getBlockState().getValue(BackpackBlock.FACING)).toYRot();
        float lidRot = 0;
        float baseRotX = 0;
        float baseRotZ = 0;
        float basePosY = 24;
        float baseScaleY = 0;
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dir));
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, isFloating ? -0.8F : -1.0F, 0.0F);


        if (blockEntity.open && blockEntity.openTicks < 10) {
            float t = ((float)blockEntity.openTicks + partialTick);
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.7F) +1;
        } else if (blockEntity.openTicks == 10){
            lidRot = 1;
        } else if (blockEntity.openTicks > 0){
            float t = ((float)blockEntity.openTicks - partialTick);
            lidRot = (float) -Math.pow(2, t -10) * Mth.sin((t - 10.75F) * 0.7F);
        }

        if (blockEntity.placeTicks <= 3 && blockEntity.newlyPlaced) {
            float t = ((float)blockEntity.placeTicks + partialTick) / 4;
            basePosY = t * t * 4 +20;
        }
        if (blockEntity.placeTicks <= 7 && blockEntity.newlyPlaced) {
            float t = ((float)blockEntity.placeTicks + partialTick) / 8;
            baseRotX = Mth.sin(t * 10) * 0.1F * (1 - t);
            baseRotZ = Mth.cos(t * 10) * 0.1F * (1 - t);
        }

        if (isFloating) {
            float t = blockEntity.floatTicks + partialTick;
            basePosY += Mth.sin((t + 20) * Mth.DEG_TO_RAD * 4) * 0.75F;
            baseRotX += Mth.sin(t * Mth.DEG_TO_RAD * 4) * 0.02F;
            baseRotZ += Mth.cos(t * Mth.DEG_TO_RAD * 4) * 0.02F;
        }

        this.lid.xRot = lidRot * 1.5F;
        this.base.xRot = baseRotX;
        this.base.zRot = baseRotZ;
        this.base.y = basePosY;

        renderBaseLayer(blockEntity, poseStack, buffer, packedLight, packedOverlay);
        if (blockEntity.getColor() != 0) {
            renderColoredLayer(blockEntity, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();

    }

    private void renderBaseLayer(BackpackBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        ResourceLocation location = blockEntity.getColor() == 0 ? TEXTURE : BASE_TEXTURE;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(location));
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    private void renderColoredLayer(BackpackBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int color = blockEntity.getColor();
        if (color == 0) {
            return;
        }
        ResourceLocation location = OVERLAY_TEXTURE;

        if (ModList.get().isLoaded("iris")) {
            irisCompatStuff(location);
        }

        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE));
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, 1.0F);
    }

    private void irisCompatStuff(ResourceLocation location) {
        if (WorldRenderingSettings.INSTANCE.getItemIds() != null) {
            CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId(location.getNamespace(), location.getPath())));
        }
    }


}
