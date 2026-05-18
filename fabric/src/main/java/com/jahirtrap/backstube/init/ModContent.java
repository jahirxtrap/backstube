package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.item.BackstubeMusicDiscItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModContent {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<SoundEvent> SOUND_EVENTS = new ArrayList<>();

    public static final SoundEvent SOUND_DISC = registerSound("disc");

    public static final Item MUSIC_DISC = registerItem("music_disc", new BackstubeMusicDiscItem(SOUND_DISC, BackstubeAPI.discProperties()));

    private static SoundEvent registerSound(String name) {
        var id = new ResourceLocation(MODID, name);
        var sound = SoundEvent.createVariableRangeEvent(id);
        var reg = Registry.register(BuiltInRegistries.SOUND_EVENT, id, sound);
        SOUND_EVENTS.add(reg);
        return reg;
    }

    private static Item registerItem(String name, Item item) {
        var reg = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MODID, name), item);
        ITEMS.add(reg);
        return reg;
    }

    public static void init() {
    }
}
