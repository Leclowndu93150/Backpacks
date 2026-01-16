package com.spydnel.backpacks.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.spydnel.backpacks.BackpackWearer;
import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.registry.BPItems;
import com.spydnel.backpacks.registry.BPLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.model.ParentType;

@OnlyIn(Dist.CLIENT)
public class BackpackLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>{
    private final ModelPart model;
    private final ModelPart parentBody;
    private static final ResourceLocation TEXTURE = new ResourceLocation(Backpacks.MODID, "textures/model/backpack.png");
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(Backpacks.MODID, "textures/model/backpack_overlay.png");

    public BackpackLayer(RenderLayerParent renderer, EntityModelSet entityModelSet) {
        super(renderer);
        this.model = entityModelSet.bakeLayer(BPLayers.BACKPACK);
        this.parentBody = this.getParentBody(renderer);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float headYaw, float headPitch) {
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);

        if (shouldRender(itemStack, livingEntity)) {
            if (ModList.get().isLoaded("figura")) {
                Backpacks.LOGGER.debug("figura is loaded");
                figuraCompatStuff(poseStack, buffer, packedLight, livingEntity, partialTicks, itemStack, this);
            } else {
                renderBaseLayer(poseStack, buffer, packedLight, livingEntity, partialTicks, itemStack, true);
            }
        }

    }

    private void figuraCompatStuff(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float partialTicks, ItemStack itemStack, BackpackLayer backpackLayer) {
        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar != null) {
            boolean shouldRender = (avatar.luaRuntime != null && avatar.luaRuntime.vanilla_model.CHESTPLATE.getVisible() != null) ? avatar.luaRuntime.vanilla_model.CHESTPLATE.getVisible() : true;
            boolean render = avatar.pivotPartRender(ParentType.ChestplatePivot, (stack) -> {
                stack.scale(16.0F, 16.0F, 16.0F);
                stack.mulPose(Axis.XP.rotationDegrees(180.0F));
                stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                renderBaseLayer(stack, buffer, packedLight, livingEntity, partialTicks, itemStack, false);
            });
            if (!render && shouldRender) { renderBaseLayer(poseStack, buffer, packedLight, livingEntity, partialTicks, itemStack, true); }
        } else {
            renderBaseLayer(poseStack, buffer, packedLight, livingEntity, partialTicks, itemStack, true);
        }
    }


    private void renderBaseLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float partialTicks, ItemStack itemStack, boolean copyPose) {
        poseStack.pushPose();
        float lidRot = 0;

        int openCount = 0;
        int openTicks = 0;
        if (livingEntity instanceof BackpackWearer wearer) {
            openCount = wearer.getBackpackOpenCount();
            openTicks = wearer.getBackpackOpenTicks();
        }

        boolean isOpen = openCount > 0;

        if (isOpen && openTicks < 10) {
            float t = ((float)openTicks + partialTicks);
            lidRot = (float) Math.pow(2, -1 * t) * Mth.sin((t - 0.75F) * 0.5F) +1;
        } else if (openTicks == 10){
            lidRot = 1;
        } else if (openTicks > 0){
            float t = ((float)openTicks - partialTicks);
            lidRot = (float) -Math.pow(2, t -10) * Mth.sin((t - 10.75F) * 0.5F);
        }

        this.model.getChild("base").getChild("lid").xRot = lidRot;
        if (copyPose) { this.model.copyFrom(parentBody); }
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(TEXTURE), false, itemStack.hasFoil());
        this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        renderColoredLayer(poseStack, buffer, packedLight, itemStack);
        poseStack.popPose();
    }

    private void renderColoredLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack itemStack) {
        int color = getColor(itemStack);
        if (color == 0) {
            return;
        }

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(OVERLAY_TEXTURE), false, itemStack.hasFoil());

        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    private static int getColor(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("display");
        if (tag != null && tag.contains("color", 99)) {
            return tag.getInt("color");
        }
        return 0;
    }

    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() == BPItems.BACKPACK.get();
    }


    protected ModelPart getParentBody(RenderLayerParent<T, HumanoidModel<T>> renderer) {
        return this.getParentModel().body;
    }
}
