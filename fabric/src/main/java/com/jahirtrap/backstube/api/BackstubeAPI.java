package com.jahirtrap.backstube.api;

import com.google.gson.JsonElement;
import com.jahirtrap.backstube.init.ModContent;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class BackstubeAPI {
    private static final String DISC_KEY = "BackstubeDisc";
    private static final String RARITY_KEY = "BackstubeRarity";
    private static final String DESCRIPTION_KEY = "BackstubeDescription";
    private static final String STACK_SIZE_KEY = "BackstubeStackSize";

    private static final Map<ResourceLocation, BackstubeMusicDisc> CODE_DISCS = new LinkedHashMap<>();

    private BackstubeAPI() {
    }

    public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey() {
        return BackstubeMusicDisc.REGISTRY_KEY;
    }

    public static Item.Properties discProperties() {
        return new Item.Properties().stacksTo(1);
    }

    public static void createDisc(ResourceLocation id, BackstubeMusicDisc disc) {
        BackstubeMusicDisc effective = disc.item().isPresent() ? disc
                : new BackstubeMusicDisc(disc.title(), disc.artist(), disc.lengthInSeconds(),
                disc.comparatorOutput(), disc.rarity(), disc.model(), disc.sound(),
                disc.stackSize(), Optional.of(id));
        CODE_DISCS.put(id, effective);
    }

    public static RecordItem createDisc(ResourceLocation id, Item.Properties properties, BackstubeMusicDisc disc) {
        return createDisc(id, properties, props -> new RecordItem(1, ModContent.SOUND_DISC, props, 0), disc);
    }

    public static <T extends RecordItem> T createDisc(ResourceLocation id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc) {
        createDisc(id, disc);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(properties));
    }

    public static Map<ResourceLocation, BackstubeMusicDisc> codeDiscs() {
        return CODE_DISCS;
    }

    public static Map<ResourceLocation, Resource> injectCodeDiscs(FileToIdConverter converter, Map<ResourceLocation, Resource> original) {
        if (CODE_DISCS.isEmpty()) return original;
        PackResources source = original.values().stream().findFirst().map(Resource::source).orElse(null);
        if (source == null) return original;
        Map<ResourceLocation, Resource> merged = new LinkedHashMap<>(original);
        for (Map.Entry<ResourceLocation, BackstubeMusicDisc> entry : CODE_DISCS.entrySet()) {
            ResourceLocation fileId = converter.idToFile(entry.getKey());
            if (merged.containsKey(fileId)) continue;
            JsonElement json = BackstubeMusicDisc.DIRECT_CODEC
                    .encodeStart(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(false, msg -> {
                    });
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            IoSupplier<InputStream> supplier = () -> new ByteArrayInputStream(bytes);
            merged.put(fileId, new Resource(source, supplier));
        }
        return merged;
    }

    public static ItemStack discStack(Holder<BackstubeMusicDisc> disc) {
        BackstubeMusicDisc value = disc.value();
        Item item = value.item()
                .filter(BuiltInRegistries.ITEM::containsKey)
                .map(BuiltInRegistries.ITEM::get)
                .orElse(ModContent.MUSIC_DISC);
        ItemStack stack = new ItemStack(item);
        applyDiscData(stack, disc);
        return stack;
    }

    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(BackstubeAPI::discStack);
    }

    public static void applyDiscData(ItemStack stack, Holder<BackstubeMusicDisc> disc) {
        BackstubeMusicDisc value = disc.value();
        ResourceLocation id = disc.unwrapKey().orElseThrow().location();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(DISC_KEY, id.toString());
        tag.putString(RARITY_KEY, value.rarity().name());
        tag.putString(DESCRIPTION_KEY, Component.Serializer.toJson(value.description()));
        tag.putInt(STACK_SIZE_KEY, value.stackSize());
    }

    public static int readStackSize(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(STACK_SIZE_KEY, Tag.TAG_INT)) return -1;
        return tag.getInt(STACK_SIZE_KEY);
    }

    public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        Optional<Holder.Reference<BackstubeMusicDisc>> opt = provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey));
        opt.ifPresent(holder -> applyDiscData(stack, holder));
        return opt.isPresent();
    }

    public static boolean isDisc(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(DISC_KEY, Tag.TAG_STRING);
    }

    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, Level level) {
        return readDisc(stack, level.registryAccess());
    }

    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, HolderLookup.Provider provider) {
        ResourceLocation id = readDiscId(stack);
        if (id == null) return Optional.empty();
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(ResourceKey.create(BackstubeMusicDisc.REGISTRY_KEY, id)))
                .map(h -> (Holder<BackstubeMusicDisc>) h);
    }

    private static ResourceLocation readDiscId(ItemStack stack) {
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

    public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .map(HolderLookup.RegistryLookup::listElements)
                .orElseGet(Stream::empty);
    }

    public static ResourceLocation discSoundLocation(BackstubeMusicDisc disc, ResourceLocation discId) {
        DiscSound config = disc.sound().orElse(DiscSound.DEFAULT);
        return config.resolveName(discId);
    }
}
