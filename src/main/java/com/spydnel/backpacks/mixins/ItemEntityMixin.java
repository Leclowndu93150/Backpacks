package com.spydnel.backpacks.mixins;

import com.spydnel.backpacks.blocks.BackpackBlockEntity;
import com.spydnel.backpacks.registry.BPBlocks;
import com.spydnel.backpacks.registry.BPItems;
import com.spydnel.backpacks.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.spydnel.backpacks.blocks.BackpackBlock.*;

@Mixin(value = ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void tick(CallbackInfo ci) {
        ItemStack itemStack = ((ItemEntity)(Object)this).getItem();
        
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
            if (((ItemEntity)(Object)this).getAge() > 0) { ((ItemEntity)(Object)this).setExtendedLifetime(); }

            this.setDeltaMovement(this.getDeltaMovement().multiply(0.9, 1.0,0.9));
            if (this.isInFluidType()) { this.getDeltaMovement().add(0.0, 20.0, 0.0); }

            Level level = level();
            BlockPos pos = this.getOnPos();
            boolean isUnobstructed = level.getBlockState(pos.above()).canBeReplaced() &&
                    (!level.getFluidState(pos.above()).isSource() || !level.getBlockState(pos.above(2)).canBeReplaced());

            if ((!level.getBlockState(pos).is(BlockTags.REPLACEABLE) || level.getFluidState(pos).isSource()) && isUnobstructed) {

                BlockState state = BPBlocks.BACKPACK.get().defaultBlockState()
                        .setValue(FACING, getDirection())
                        .setValue(FLOATING, level.getFluidState(pos).isSource() && !level.getFluidState(pos.above()).isSource())
                        .setValue(WATERLOGGED, level.getFluidState(pos.above()).getType() == Fluids.WATER);

                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos.above(), state);
                    BlockEntity blockEntity = level.getBlockEntity(pos.above());
                    if (blockEntity instanceof BackpackBlockEntity backpackBlockEntity) {
                        if (itemStack.hasTag() && itemStack.getTag().contains("BlockEntityTag")) {
                            backpackBlockEntity.load(itemStack.getTag().getCompound("BlockEntityTag"));
                        }
                        CompoundTag displayTag = itemStack.getTagElement("display");
                        if (displayTag != null && displayTag.contains("color", 99)) {
                            backpackBlockEntity.setColor(displayTag.getInt("color"));
                        }
                    }
                    level.playSound(null, pos.above(), BPSounds.BACKPACK_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                this.discard();
            }
        }
    }
}
