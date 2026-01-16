package com.spydnel.backpacks.registry;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.blocks.BackpackBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BPBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Backpacks.MODID);

    public static final RegistryObject<BackpackBlock> BACKPACK = BLOCKS.register(
            "backpack", () -> new BackpackBlock(BlockBehaviour.Properties.copy(Blocks.BROWN_WOOL)
                    .sound(new SoundType(1.0F, 1.0F,
                            SoundEvents.WOOL_BREAK,
                            SoundEvents.WOOL_STEP,
                            BPSounds.BACKPACK_PLACE.get(),
                            SoundEvents.WOOL_HIT,
                            SoundEvents.WOOL_FALL))
                    .forceSolidOn()));
}
