package com.jahirtrap.backstube.api;

import com.jahirtrap.backstube.init.ModContent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Optional;
import java.util.stream.Stream;

public class BackstubeAPI {
    private static final String DISC_KEY = "BackstubeDisc";
    private static final String RARITY_KEY = "BackstubeRarity";
    private static final String DESCRIPTION_KEY = "BackstubeDescription";

    private BackstubeAPI() {
    }

    public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey() {
        return BackstubeMusicDisc.REGISTRY_KEY;
    }

    public static Item.Properties discProperties() {
        return new Item.Properties().stacksTo(1);
    }

    public static ItemStack discStack(Holder<BackstubeMusicDisc> disc) {
        ItemStack stack = new ItemStack(ModContent.MUSIC_DISC.get());
        BackstubeMusicDisc value = disc.value();
        ResourceLocation id = disc.unwrapKey().orElseThrow().location();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(DISC_KEY, id.toString());
        tag.putString(RARITY_KEY, value.rarity().name());
        tag.putString(DESCRIPTION_KEY, Component.Serializer.toJson(value.description()));
        return stack;
    }

    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider holders) {
        return holders.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(BackstubeAPI::discStack);
    }

    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, HolderLookup.Provider holders) {
        ResourceLocation id = readDiscId(stack);
        if (id == null) return Optional.empty();
        return holders.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(ResourceKey.create(BackstubeMusicDisc.REGISTRY_KEY, id)))
                .map(h -> (Holder<BackstubeMusicDisc>) h);
    }

    public static ResourceLocation readDiscId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DISC_KEY, Tag.TAG_STRING)) return null;
        return ResourceLocation.tryParse(tag.getString(DISC_KEY));
    }

    public static Component readDescription(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DESCRIPTION_KEY, Tag.TAG_STRING)) return null;
        return Component.Serializer.fromJson(tag.getString(DESCRIPTION_KEY));
    }

    public static Rarity readRarity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(RARITY_KEY, Tag.TAG_STRING)) return Rarity.COMMON;
        try {
            return Rarity.valueOf(tag.getString(RARITY_KEY));
        } catch (IllegalArgumentException e) {
            return Rarity.COMMON;
        }
    }

    public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider holders) {
        return holders.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .map(HolderLookup.RegistryLookup::listElements)
                .orElseGet(Stream::empty);
    }

    public static ResourceLocation discSoundLocation(BackstubeMusicDisc disc, ResourceLocation discId) {
        DiscSound config = disc.sound().orElse(DiscSound.DEFAULT);
        return config.resolveName(discId);
    }
}
