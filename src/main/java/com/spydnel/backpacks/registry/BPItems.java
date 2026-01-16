package com.spydnel.backpacks.registry;

import com.spydnel.backpacks.Backpacks;
import com.spydnel.backpacks.items.BackpackItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Backpacks.MODID);

    public static final RegistryObject<BackpackItem> BACKPACK = ITEMS.register("backpack",
            () -> new BackpackItem(BPBlocks.BACKPACK.get(), new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()));
}
