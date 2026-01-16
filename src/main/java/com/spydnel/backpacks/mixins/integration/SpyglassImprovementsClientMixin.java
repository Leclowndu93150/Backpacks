package com.spydnel.backpacks.mixins.integration;

import com.spydnel.backpacks.registry.BPItems;
import me.juancarloscp52.spyglass_improvements.client.SpyglassImprovementsClient;
import me.juancarloscp52.spyglass_improvements.mixin.MinecraftClientInvoker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpyglassImprovementsClient.class)
public abstract class SpyglassImprovementsClientMixin {

    @Shadow
    public static KeyMapping useSpyglass;

    @Shadow
    private void forceUseSpyglass(LocalPlayer player) { }

    @Inject(method = "onClientTick", at = @At("HEAD"), remap = false)
    public void onClientTick(Minecraft client, CallbackInfo ci) {
        if (client.player != null &&
                client.gameMode != null && useSpyglass.isDown() &&
                ((MinecraftClientInvoker)client).getItemUseCooldown() == 0 &&
                !client.player.isUsingItem() &&
                client.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == BPItems.BACKPACK.get() &&
                backpackContainsSpyglass(client.player.getItemBySlot(EquipmentSlot.CHEST))
        ) {
            forceUseSpyglass(client.player);
        }
    }

    private boolean backpackContainsSpyglass(ItemStack backpack) {
        if (!backpack.hasTag()) {
            return false;
        }
        CompoundTag tag = backpack.getTag();
        if (!tag.contains("BlockEntityTag")) {
            return false;
        }
        CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
        if (!blockEntityTag.contains("Items")) {
            return false;
        }
        ListTag items = blockEntityTag.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemTag = items.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            if (stack.is(Items.SPYGLASS)) {
                return true;
            }
        }
        return false;
    }
}
