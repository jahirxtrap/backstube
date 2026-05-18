package com.jahirtrap.backstube.api;

import com.jahirtrap.backstube.init.ModComponents;
import com.jahirtrap.backstube.init.ModContent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

import java.util.Optional;
import java.util.stream.Stream;

public class BackstubeAPI {
    private BackstubeAPI() {
    }

    public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey() {
        return BackstubeMusicDisc.REGISTRY_KEY;
    }

    public static DataComponentType<Holder<BackstubeMusicDisc>> discComponent() {
        return ModComponents.DISC.get();
    }

    public static Item.Properties discProperties(ResourceKey<BackstubeMusicDisc> diskKey) {
        return new Item.Properties()
                .stacksTo(1)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, diskKey.identifier()));
    }

    public static ItemStack discStack(Holder<BackstubeMusicDisc> disc, HolderLookup.Provider holders) {
        ItemStack stack = new ItemStack(ModContent.MUSIC_DISC.get());
        stack.set(ModComponents.DISC, disc);
        stack.set(DataComponents.RARITY, disc.value().rarity());
        Identifier id = disc.unwrapKey().orElseThrow().identifier();
        holders.lookup(Registries.JUKEBOX_SONG)
                .flatMap(reg -> reg.get(ResourceKey.create(Registries.JUKEBOX_SONG, id)))
                .ifPresent(song -> stack.set(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(song))));
        return stack;
    }

    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider holders) {
        return holders.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(holder -> discStack(holder, holders));
    }

    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.DISC));
    }

    public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider holders) {
        return holders.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .map(HolderLookup.RegistryLookup::listElements)
                .orElseGet(Stream::empty);
    }
}
