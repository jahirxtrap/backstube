package com.jahirtrap.backstube.api;

import com.google.gson.JsonElement;
import com.jahirtrap.backstube.init.ModComponents;
import com.jahirtrap.backstube.init.ModContent;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class BackstubeAPI {
    private static final Map<ResourceLocation, BackstubeMusicDisc> CODE_DISCS = new LinkedHashMap<>();

    private BackstubeAPI() {
    }

    public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey() {
        return BackstubeMusicDisc.REGISTRY_KEY;
    }

    public static DataComponentType<Holder<BackstubeMusicDisc>> discComponent() {
        return ModComponents.DISC;
    }

    public static Item.Properties discProperties(ResourceKey<BackstubeMusicDisc> diskKey) {
        return new Item.Properties()
                .stacksTo(1)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, diskKey.location()));
    }

    public static void createDisc(ResourceLocation id, BackstubeMusicDisc disc) {
        BackstubeMusicDisc effective = disc.item().isPresent() ? disc
                : new BackstubeMusicDisc(disc.title(), disc.artist(), disc.lengthInSeconds(),
                disc.comparatorOutput(), disc.rarity(), disc.model(), disc.sound(),
                disc.stackSize(), Optional.of(id));
        CODE_DISCS.put(id, effective);
    }

    public static Item createDisc(ResourceLocation id, Item.Properties properties, BackstubeMusicDisc disc) {
        return createDisc(id, properties, Item::new, disc);
    }

    public static <T extends Item> T createDisc(ResourceLocation id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc) {
        createDisc(id, disc);
        Item.Properties enriched = properties
                .component(ModComponents.DISC, Holder.direct(disc))
                .component(DataComponents.RARITY, disc.rarity())
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, id));
        if (disc.stackSize() != 64) {
            enriched = enriched.component(DataComponents.MAX_STACK_SIZE, disc.stackSize());
        }
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(enriched));
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
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode code-registered disc " + entry.getKey() + ": " + msg));
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            IoSupplier<InputStream> supplier = () -> new ByteArrayInputStream(bytes);
            merged.put(fileId, new Resource(source, supplier));
        }
        return merged;
    }

    public static ItemStack discStack(Holder<BackstubeMusicDisc> disc, HolderLookup.Provider provider) {
        BackstubeMusicDisc value = disc.value();
        Item item = value.item()
                .flatMap(BuiltInRegistries.ITEM::getHolder)
                .map(Holder::value)
                .orElse(ModContent.MUSIC_DISC);
        ItemStack stack = new ItemStack(item);
        applyDiscData(stack, disc, provider);
        return stack;
    }

    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(holder -> discStack(holder, provider));
    }

    public static void applyDiscData(ItemStack stack, Holder<BackstubeMusicDisc> disc, HolderLookup.Provider provider) {
        BackstubeMusicDisc value = disc.value();
        stack.set(ModComponents.DISC, disc);
        stack.set(DataComponents.RARITY, value.rarity());
        if (value.stackSize() != 64) {
            stack.set(DataComponents.MAX_STACK_SIZE, value.stackSize());
        }
        ResourceLocation id = disc.unwrapKey().orElseThrow().location();
        provider.lookup(Registries.JUKEBOX_SONG)
                .flatMap(reg -> reg.get(ResourceKey.create(Registries.JUKEBOX_SONG, id)))
                .ifPresent(song -> stack.set(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(song), true)));
    }

    public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        Optional<Holder.Reference<BackstubeMusicDisc>> opt = provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey));
        opt.ifPresent(holder -> applyDiscData(stack, holder, provider));
        return opt.isPresent();
    }

    public static boolean isDisc(ItemStack stack) {
        return stack.has(ModComponents.DISC);
    }

    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.DISC));
    }

    public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .map(HolderLookup.RegistryLookup::listElements)
                .orElseGet(Stream::empty);
    }
}
