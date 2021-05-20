package com.wynntils;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WynntilsSounds {
    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);
    
    public static final RegistryObject<SoundEvent> HORSE_WHISTLE = SOUNDS.register("horse_whistle", () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, "horse_whistle")));
    public static final RegistryObject<SoundEvent> WAR_HORN = SOUNDS.register("war_horn", () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, "war_horn")));
    public static final RegistryObject<SoundEvent> QUESTBOOK_UPDATE = SOUNDS.register("questbook_update", () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, "questbook_update")));
    public static final RegistryObject<SoundEvent> QUESTBOOK_PAGE = SOUNDS.register("questbook_page", () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, "questbook_page")));
    public static final RegistryObject<SoundEvent> QUESTBOOK_OPENING = SOUNDS.register("questbook_opening", () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, "questbook_opening")));
}
