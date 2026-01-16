package com.spydnel.backpacks.events;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.blocks.BackpackBlockEntity;
import com.spydnel.backpacks.registry.BPBlocks;
import com.spydnel.backpacks.registry.BPItems;
import com.spydnel.backpacks.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.spydnel.backpacks.blocks.BackpackBlock.FACING;
import static com.spydnel.backpacks.blocks.BackpackBlock.WATERLOGGED;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Backpacks.MODID)
public class BackpackPickupEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        BlockPos pos = event.getPos();
        Block block = level.getBlockState(pos).getBlock();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        ItemStack heldItem = event.getItemStack();
        ItemStack chestSlotItem = player.getItemBySlot(EquipmentSlot.CHEST);

        boolean hasBackpack = chestSlotItem.getItem() == BPItems.BACKPACK.get();
        boolean hasChestPlate = !chestSlotItem.isEmpty();
        boolean isAbove = (pos.above().getY() > player.getEyeY());
        boolean isUnobstructed = level.isUnobstructed(BPBlocks.BACKPACK.get().defaultBlockState(), pos.above(),
                CollisionContext.of(player)) && level.getBlockState(pos.above()).canBeReplaced();
        
        if (player.isShiftKeyDown() && !hasChestPlate && block == BPBlocks.BACKPACK.get() && blockEntity instanceof BackpackBlockEntity backpackBlockEntity) {

            player.swing(hand);
            ItemStack itemstack = new ItemStack(BPBlocks.BACKPACK.get());

            CompoundTag blockEntityTag = backpackBlockEntity.saveWithoutMetadata();
            blockEntityTag.remove("Color");
            if (!blockEntityTag.isEmpty()) {
                itemstack.getOrCreateTag().put("BlockEntityTag", blockEntityTag);
            }

            int color = backpackBlockEntity.getColor();
            if (color != 0) {
                CompoundTag displayTag = itemstack.getOrCreateTagElement("display");
                displayTag.putInt("color", color);
            }

            player.setItemSlot(EquipmentSlot.CHEST, itemstack);
            addParticles(level, pos);

            if (!level.isClientSide) {
                level.removeBlockEntity(pos);
                level.removeBlock(pos, false);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }
        
        if (player.isShiftKeyDown() && heldItem.isEmpty() && hasBackpack && event.getFace() == Direction.UP && !isAbove && isUnobstructed) {

            player.swing(hand);
            player.swingingArm = InteractionHand.MAIN_HAND;

            BlockState state = BPBlocks.BACKPACK.get().defaultBlockState()
                    .setValue(FACING, player.getDirection())
                    .setValue(WATERLOGGED, level.getFluidState(pos.above()).getType() == Fluids.WATER);

            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos.above(), state);
                BlockEntity newBlockEntity = level.getBlockEntity(pos.above());
                if (newBlockEntity instanceof BackpackBlockEntity backpackBlockEntity) {
                    if (chestSlotItem.hasTag() && chestSlotItem.getTag().contains("BlockEntityTag")) {
                        backpackBlockEntity.load(chestSlotItem.getTag().getCompound("BlockEntityTag"));
                    }
                    CompoundTag displayTag = chestSlotItem.getTagElement("display");
                    if (displayTag != null && displayTag.contains("color", 99)) {
                        backpackBlockEntity.setColor(displayTag.getInt("color"));
                    }
                }

                chestSlotItem.shrink(1);
                level.playSound(null, pos.above(), BPSounds.BACKPACK_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }
    }

    //ARMOR SWAPPING
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Item item = event.getItemStack().getItem();
        EquipmentSlot slot = null;

        if (item instanceof ArmorItem) { slot = ((ArmorItem)item).getEquipmentSlot(); }
        if (item instanceof Equipable) { slot = ((Equipable)item).getEquipmentSlot(); }

        if (slot == EquipmentSlot.CHEST && event.getEntity().getItemBySlot(EquipmentSlot.CHEST).getItem() == BPItems.BACKPACK.get()) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onItemEntityPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack itemStack = itemEntity.getItem();

        boolean hasItems = false;
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            if (tag.contains("BlockEntityTag")) {
                CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Items")) {
                    hasItems = !blockEntityTag.getList("Items", 10).isEmpty();
                }
            }
        }

        if (itemStack.getItem() == BPItems.BACKPACK.get() && hasItems) {
            Player player = event.getEntity();
            if (player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && !itemEntity.hasPickUpDelay()) {
                player.setItemSlot(EquipmentSlot.CHEST, itemStack);
                player.take(itemEntity, 1);
                itemEntity.discard();
                player.awardStat(Stats.ITEM_PICKED_UP.get(itemStack.getItem()), 1);
                player.onItemPickup(itemEntity);
            }
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    private static void addParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 4; i++) {
            level.addParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0, 0);
        }
    }
}
