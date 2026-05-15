package com.jahirtrap.backstube.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<SoundEvent> SOUND_EVENTS = new ArrayList<>();

    public static final Item MUSIC_DISC = registerItem("music_disc", Item::new, new Item.Properties().stacksTo(1));

    public static final SoundEvent SOUND_IN_THE_QUARTZ_POOL = registerSound("music_disc.in_the_quartz_pool");

    private static SoundEvent registerSound(String name) {
        var key = ResourceKey.create(Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MODID, name));
        var sound = SoundEvent.createVariableRangeEvent(key.identifier());
        var reg = Registry.register(BuiltInRegistries.SOUND_EVENT, key, sound);
        SOUND_EVENTS.add(reg);
        return reg;
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> function, Item.Properties itemProp) {
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name));
        var item = Registry.register(BuiltInRegistries.ITEM, key, function.apply(itemProp.setId(key)));
        ITEMS.add(item);
        return item;
    }

    public static void init() {
    }
}
