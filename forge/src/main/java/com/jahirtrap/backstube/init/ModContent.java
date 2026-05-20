package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final RegistryObject<SoundEvent> SOUND_DISC = SOUND_EVENTS.register("disc", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "disc")));

    public static final RegistryObject<Item> MUSIC_DISC = ITEMS.register("music_disc", () -> new RecordItem(1, SOUND_DISC.get(), BackstubeAPI.discProperties(), 0));

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
    }
}
