package com.spydnel.backpacks.mixins;

import com.spydnel.backpacks.BackpackWearer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements BackpackWearer {

    @Unique
    private int backpacks$openCount = 0;

    @Unique
    private int backpacks$openTicks = 0;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo ci) {
        if (backpacks$openCount > 0 && backpacks$openTicks < 10) {
            backpacks$openTicks++;
        }
        if (backpacks$openCount == 0 && backpacks$openTicks > 0) {
            backpacks$openTicks--;
        }
    }

    @Override
    public void onBackpackOpen() {
        backpacks$openCount++;
    }

    @Override
    public void onBackpackClose() {
        if (backpacks$openCount > 0) {
            backpacks$openCount--;
        }
    }

    @Override
    public int getBackpackOpenCount() {
        return backpacks$openCount;
    }

    @Override
    public int getBackpackOpenTicks() {
        return backpacks$openTicks;
    }
}
