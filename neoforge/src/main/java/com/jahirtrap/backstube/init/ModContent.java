package com.jahirtrap.backstube.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);

    public static final DeferredItem<Item> MUSIC_DISC_IN_THE_QUARTZ_POOL = registerDisc("in_the_quartz_pool", Rarity.RARE);

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, name)));
    }

    private static DeferredItem<Item> registerItem(String name, Function<Item.Properties, Item> function, Item.Properties itemProp) {
        return ITEMS.register(name, () -> function.apply(itemProp.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, name)))));
    }

    private static DeferredItem<Item> registerDisc(String name, Rarity rarity) {
        registerSound("music_disc." + name);
        var songKey = ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(MODID, name));
        return registerItem("music_disc_" + name, Item::new, new Item.Properties().stacksTo(1).rarity(rarity).jukeboxPlayable(songKey));
    }

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
    }
}
