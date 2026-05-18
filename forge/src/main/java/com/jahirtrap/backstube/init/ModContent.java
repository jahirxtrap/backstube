package com.jahirtrap.backstube.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final RegistryObject<Item> MUSIC_DISC = registerItem("music_disc", Item::new, new Item.Properties().stacksTo(1));

    public static final RegistryObject<SoundEvent> SOUND_DISC = registerSound("disc");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, name)));
    }

    private static RegistryObject<Item> registerItem(String name, Function<Item.Properties, Item> function, Item.Properties itemProp) {
        return ITEMS.register(name, () -> function.apply(itemProp));
    }

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
    }
}
