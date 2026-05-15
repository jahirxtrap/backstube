package com.jahirtrap.backstube.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final RegistryObject<Item> MUSIC_DISC = registerItem("music_disc", Item::new, new Item.Properties().stacksTo(1));

    public static final RegistryObject<SoundEvent> SOUND_BRIGHT_CRASH = registerSound("music_disc.bright_crash");
    public static final RegistryObject<SoundEvent> SOUND_HOLLOW_PULSE = registerSound("music_disc.hollow_pulse");
    public static final RegistryObject<SoundEvent> SOUND_IN_THE_QUARTZ_POOL = registerSound("music_disc.in_the_quartz_pool");
    public static final RegistryObject<SoundEvent> SOUND_QUIET_PINES = registerSound("music_disc.quiet_pines");
    public static final RegistryObject<SoundEvent> SOUND_STEEL_TEARS = registerSound("music_disc.steel_tears");
    public static final RegistryObject<SoundEvent> SOUND_TWILIGHT_HIKE = registerSound("music_disc.twilight_hike");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, name)));
    }

    private static RegistryObject<Item> registerItem(String name, Function<Item.Properties, Item> function, Item.Properties itemProp) {
        return ITEMS.register(name, () -> function.apply(itemProp.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name)))));
    }

    public static void init(BusGroup bus) {
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
    }
}
