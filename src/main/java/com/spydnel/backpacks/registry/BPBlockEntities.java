package com.spydnel.backpacks.registry;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.blocks.BackpackBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Backpacks.MODID);

    public static final RegistryObject<BlockEntityType<BackpackBlockEntity>> BACKPACK = BLOCK_ENTITY_TYPES.register(
            "backpack",
            () -> BlockEntityType.Builder.of(BackpackBlockEntity::new, BPBlocks.BACKPACK.get()).build(null));
}
