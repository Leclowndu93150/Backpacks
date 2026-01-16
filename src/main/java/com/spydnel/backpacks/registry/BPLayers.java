package com.spydnel.backpacks.registry;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.models.BackpackBlockRenderer;
import com.spydnel.backpacks.models.BackpackLayer;
import com.spydnel.backpacks.models.BackpackModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Backpacks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BPLayers {

    public static final ModelLayerLocation BACKPACK = new ModelLayerLocation(new ResourceLocation(Backpacks.MODID, "backpack"), "main");
    public static final ModelLayerLocation BACKPACK_BLOCK = new ModelLayerLocation(new ResourceLocation(Backpacks.MODID, "backpack_block"), "main");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    BPItems.BACKPACK.get(),
                    new ResourceLocation(Backpacks.MODID, "dyed"),
                    (stack, level, player, seed) -> isDyed(stack)
            );
        });
    }

    private static float isDyed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("display") && stack.getTag().getCompound("display").contains("color") ? 1 : 0;
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BACKPACK, BackpackModel::createBodyLayer);
        event.registerLayerDefinition(BACKPACK_BLOCK, BackpackBlockRenderer::createBodyLayer);
    }


    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BPBlockEntities.BACKPACK.get(),
                BackpackBlockRenderer::new
        );
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new BackpackLayer<>(playerRenderer, event.getEntityModels()));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            try {
                LivingEntityRenderer renderer = event.getRenderer((EntityType<? extends LivingEntity>) entityType);
                if (renderer instanceof HumanoidMobRenderer humanoidMobRenderer) {
                    humanoidMobRenderer.addLayer(new BackpackLayer(humanoidMobRenderer, event.getEntityModels()));
                } else if (renderer instanceof ArmorStandRenderer armorStandRenderer) {
                    armorStandRenderer.addLayer(new BackpackLayer<>(armorStandRenderer, event.getEntityModels()));
                }
            } catch (ClassCastException ignored) {
                // Not a LivingEntity type, skip
            }
        }
    }
}
