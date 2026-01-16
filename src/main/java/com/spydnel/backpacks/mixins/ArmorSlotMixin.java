package com.spydnel.backpacks.mixins;

import com.spydnel.backpacks.registry.BPItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class ArmorSlotMixin {

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public int index;

    @Inject(
            method = "mayPickup",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mayPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot)(Object)this;

        if (!(self.container instanceof Inventory)) {
            return;
        }

        int inventoryIndex = self.getContainerSlot();
        if (inventoryIndex < 36 || inventoryIndex > 39) {
            return;
        }

        ItemStack item = this.getItem();
        if (item.getItem() != BPItems.BACKPACK.get()) {
            return;
        }

        boolean hasItems = false;
        if (item.hasTag()) {
            CompoundTag tag = item.getTag();
            if (tag.contains("BlockEntityTag")) {
                CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Items")) {
                    hasItems = !blockEntityTag.getList("Items", 10).isEmpty();
                }
            }
        }

        if (hasItems) {
            cir.setReturnValue(false);
        }
    }
}
