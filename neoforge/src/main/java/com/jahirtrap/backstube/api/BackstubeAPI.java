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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class BackstubeAPI {
    private static final Map<Identifier, BackstubeMusicDisc> CODE_DISCS = new LinkedHashMap<>();

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

    public static void createDisc(Identifier id, BackstubeMusicDisc disc) {
        BackstubeMusicDisc effective = disc.item().isPresent() ? disc
                : new BackstubeMusicDisc(disc.title(), disc.artist(), disc.lengthInSeconds(),
                disc.comparatorOutput(), disc.rarity(), disc.model(), disc.sound(),
                disc.stackSize(), Optional.of(id));
        CODE_DISCS.put(id, effective);
    }

    public static DeferredItem<Item> createDisc(DeferredRegister.Items register, Identifier id, Item.Properties properties, BackstubeMusicDisc disc) {
        return createDisc(register, id, properties, Item::new, disc);
    }

    public static <T extends Item> DeferredItem<T> createDisc(DeferredRegister.Items register, Identifier id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        return register.register(id.getPath(), key -> {
            createDisc(id, disc);
            Item.Properties enriched = properties
                    .component(ModComponents.DISC, Holder.direct(disc))
                    .component(DataComponents.RARITY, disc.rarity())
                    .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, id));
            if (disc.stackSize() != 64) {
                enriched = enriched.component(DataComponents.MAX_STACK_SIZE, disc.stackSize());
            }
            return factory.apply(enriched.setId(itemKey));
        });
    }

    public static Map<Identifier, BackstubeMusicDisc> codeDiscs() {
        return CODE_DISCS;
    }

    public static Map<Identifier, Resource> injectCodeDiscs(FileToIdConverter converter, Map<Identifier, Resource> original) {
        if (CODE_DISCS.isEmpty()) return original;
        PackResources source = original.values().stream().findFirst().map(Resource::source).orElse(null);
        if (source == null) return original;
        Map<Identifier, Resource> merged = new LinkedHashMap<>(original);
        for (Map.Entry<Identifier, BackstubeMusicDisc> entry : CODE_DISCS.entrySet()) {
            Identifier fileId = converter.idToFile(entry.getKey());
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
                .flatMap(BuiltInRegistries.ITEM::get)
                .map(Holder::value)
                .orElse(ModContent.MUSIC_DISC.get());
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
        Identifier id = disc.unwrapKey().orElseThrow().identifier();
        provider.lookup(Registries.JUKEBOX_SONG)
                .flatMap(reg -> reg.get(ResourceKey.create(Registries.JUKEBOX_SONG, id)))
                .ifPresent(song -> stack.set(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(song))));
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
