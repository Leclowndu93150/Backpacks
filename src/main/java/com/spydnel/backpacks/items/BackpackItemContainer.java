package com.spydnel.backpacks.items;

import com.spydnel.backpacks.networking.BackpackNetworking;
import com.spydnel.backpacks.networking.BackpackOpenPacket;
import com.spydnel.backpacks.registry.BPItems;
import com.spydnel.backpacks.registry.BPSounds;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class BackpackItemContainer extends SimpleContainer {
    LivingEntity target;
    Player player;
    ItemStack itemStack;
    Level level;

    public BackpackItemContainer(LivingEntity target, Player player) {
        super(27);
        this.target = target;
        this.player = player;
        itemStack = target.getItemBySlot(EquipmentSlot.CHEST);
        level = target.level();

        loadFromItem(itemStack);
    }

    private void loadFromItem(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
            CompoundTag blockEntityTag = stack.getTag().getCompound("BlockEntityTag");
            if (blockEntityTag.contains("Items")) {
                NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(blockEntityTag, items);
                for (int i = 0; i < items.size(); i++) {
                    this.setItem(i, items.get(i));
                }
            }
        }
    }

    private void saveToItem(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        for (int i = 0; i < this.getContainerSize(); i++) {
            items.set(i, this.getItem(i));
        }

        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag blockEntityTag = tag.contains("BlockEntityTag") ? tag.getCompound("BlockEntityTag") : new CompoundTag();
        ContainerHelper.saveAllItems(blockEntityTag, items);
        tag.put("BlockEntityTag", blockEntityTag);
    }

    public boolean stillValid(Player player) {
        return
                target != null &&
                itemStack.getItem() == BPItems.BACKPACK.get() &&
                player.distanceTo(target) < 5;
    }

    public void setChanged() {
        saveToItem(target.getItemBySlot(EquipmentSlot.CHEST));
        super.setChanged();
    }

    @Override
    public void startOpen(Player player) {
        if (!target.level().isClientSide) {
            BackpackNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new BackpackOpenPacket(true, target.getId()));
        }
        target.level().playSound(null, target.blockPosition(), BPSounds.BACKPACK_OPEN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        if (!target.level().isClientSide) {
            BackpackNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new BackpackOpenPacket(false, target.getId()));
        }
        target.level().playSound(null, target.blockPosition(), BPSounds.BACKPACK_CLOSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        super.stopOpen(player);
    }
}
