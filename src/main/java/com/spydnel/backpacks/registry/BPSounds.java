package com.spydnel.backpacks.registry;

import com.spydnel.backpacks.Backpacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BPSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Backpacks.MODID);

    public static final RegistryObject<SoundEvent> BACKPACK_PLACE = SOUND_EVENTS.register("block.backpack.place",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Backpacks.MODID, "block.backpack.place")));
    public static final RegistryObject<SoundEvent> BACKPACK_OPEN = SOUND_EVENTS.register("block.backpack.open",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Backpacks.MODID, "block.backpack.open")));
    public static final RegistryObject<SoundEvent> BACKPACK_CLOSE = SOUND_EVENTS.register("block.backpack.close",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Backpacks.MODID, "block.backpack.close")));
    public static final RegistryObject<SoundEvent> BACKPACK_EQUIP = SOUND_EVENTS.register("item.backpack.equip",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Backpacks.MODID, "item.backpack.equip")));
}
