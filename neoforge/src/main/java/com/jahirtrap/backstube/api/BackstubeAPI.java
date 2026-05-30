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
import net.minecraft.server.packs.repository.KnownPack;
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

/**
 * Public entry point for the Backstube music disc framework.
 * <p>
 * Backstube unifies every music disc into a single {@code backstube:music_disc}
 * item driven by a {@link BackstubeMusicDisc} data component. Mods, datapacks
 * and resource packs add new discs by registering a {@link BackstubeMusicDisc}
 * under a unique id; the disc's audio file is auto-resolved from that id.
 * <p>
 * Discs can be added in three ways:
 * <ul>
 *   <li><b>Data-driven</b> &mdash; ship a JSON file under
 *       {@code data/<namespace>/backstube/music_disc/<path>.json} plus an OGG
 *       Vorbis mono audio asset; no Java code required.</li>
 *   <li><b>Pure Java</b> &mdash; register everything in code via
 *       {@link #createDisc(Identifier, BackstubeMusicDisc)} or one of its
 *       item-aware overloads.</li>
 *   <li><b>Hybrid</b> &mdash; register a custom item with
 *       {@link #discProperties(ResourceKey)} and provide the disc data in
 *       JSON, optionally pointing back to the item via the {@code item}
 *       field.</li>
 * </ul>
 * <p>
 * All methods are static; this class is not instantiable.
 *
 * @see BackstubeMusicDisc
 * @see DiscSound
 * @since 0.1.2
 */
public class BackstubeAPI {
    private static final Map<Identifier, BackstubeMusicDisc> CODE_DISCS = new LinkedHashMap<>();

    private BackstubeAPI() {
    }

    /**
     * Returns the registry key for the {@code backstube:music_disc} registry.
     * <p>
     * Use this to declare disc-related {@code ResourceKey} instances, register
     * tags under {@code data/<namespace>/tags/backstube/music_disc/} or hook datapack
     * registry handlers.
     *
     * @return the registry key; never {@code null}
     * @see BackstubeMusicDisc#REGISTRY_KEY
     * @since 0.1.2
     */
    public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey() {
        return BackstubeMusicDisc.REGISTRY_KEY;
    }

    /**
     * Returns the {@code backstube:disc} data component type attached to
     * {@code backstube:music_disc} item stacks.
     * <p>
     * The component value is a {@link Holder} that resolves to the registered
     * {@link BackstubeMusicDisc}. Read it with
     * {@code stack.get(BackstubeAPI.discComponent())} or use
     * {@link #readDisc(ItemStack)} for a null-safe wrapper.
     *
     * @return the data component type; never {@code null}
     * @see #readDisc(ItemStack)
     * @since 0.1.2
     */
    public static DataComponentType<Holder<BackstubeMusicDisc>> discComponent() {
        return ModComponents.DISC.get();
    }

    /**
     * Builds {@link Item.Properties} pre-configured for an item that should be
     * recognised as a jukebox-playable disc bound to {@code diskKey}.
     * <p>
     * The returned properties have:
     * <ul>
     *   <li>{@code stacksTo(1)};</li>
     *   <li>{@code jukeboxPlayable} set to the matching jukebox song.</li>
     * </ul>
     * <p>
     * The {@code backstube:disc} and {@code minecraft:rarity} components are not
     * applied to the properties returned here; they are stamped on the stack at
     * creation time by
     * {@link #applyDiscData(ItemStack, Holder, HolderLookup.Provider)} (the
     * helper invoked by {@link #discStack(Holder, HolderLookup.Provider)}). When
     * registering a custom item subclass via this method, the disc data must
     * still exist either as a JSON file or as a code-registered entry via
     * {@link #createDisc(Identifier, BackstubeMusicDisc)}.
     *
     * @param diskKey the disc registry key the item should be bound to;
     *                must not be {@code null}
     * @return a fresh {@link Item.Properties} ready to be passed to an item
     * constructor; never {@code null}
     * @see #createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)
     * @since 0.1.2
     */
    public static Item.Properties discProperties(ResourceKey<BackstubeMusicDisc> diskKey) {
        return new Item.Properties()
                .stacksTo(1)
                .jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, diskKey.identifier()));
    }

    /**
     * Registers a code-based disc backed by the generic {@code backstube:music_disc}
     * item.
     * <p>
     * The disc is added to an in-memory map and merged into the
     * {@code backstube:music_disc} data registry at datapack load time. If a JSON
     * file with the same id exists, the JSON entry takes precedence over the
     * code-registered one.
     * <p>
     * The supplied disc is rewritten so its {@link BackstubeMusicDisc#item()}
     * field always equals {@code id}; any user-supplied value of {@code item} on
     * the input is ignored.
     *
     * @param id   the disc id, e.g. {@code Identifier.fromNamespaceAndPath("example", "cool_song")};
     *             must not be {@code null}
     * @param disc the disc data, typically built with
     *             {@link BackstubeMusicDisc#builder()}; must not be {@code null}
     * @see #createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)
     * @since 0.1.3
     */
    public static void createDisc(Identifier id, BackstubeMusicDisc disc) {
        BackstubeMusicDisc effective = disc.item().isPresent() ? disc
                : new BackstubeMusicDisc(disc.title(), disc.artist(), disc.lengthInSeconds(),
                disc.comparatorOutput(), disc.rarity(), disc.model(), disc.sound(),
                disc.stackSize(), Optional.of(id));
        CODE_DISCS.put(id, effective);
    }

    /**
     * Registers a code-based disc together with a custom {@link Item} bound to
     * its registry entry.
     * <p>
     * Equivalent to calling {@link #createDisc(Identifier, BackstubeMusicDisc)}
     * and then registering an {@link Item} under {@code id} with the supplied
     * {@code properties} enriched with the {@code backstube:disc} component,
     * {@code minecraft:rarity} component and {@code jukeboxPlayable} binding.
     * <p>
     * The item is deferred through the caller-provided
     * {@link DeferredRegister.Items} and constructed lazily when the NeoForge
     * registry event fires; assign the returned {@link DeferredItem} to a
     * {@code static final} field so static initialization order is preserved.
     *
     * @param register   the mod's item {@link DeferredRegister.Items}; must not
     *                   be {@code null} and must already be attached to the mod
     *                   event bus
     * @param id         the disc id, also used as the item id
     * @param properties base item properties; {@code stacksTo} and the disc
     *                   components are applied automatically
     * @param disc       the disc data
     * @return a {@code DeferredItem<Item>} pointing at the deferred item;
     * never {@code null}
     * @see #createDisc(DeferredRegister.Items, Identifier, Item.Properties, Function, BackstubeMusicDisc)
     * @since 0.1.3
     */
    public static DeferredItem<Item> createDisc(DeferredRegister.Items register, Identifier id, Item.Properties properties, BackstubeMusicDisc disc) {
        return createDisc(register, id, properties, Item::new, disc);
    }

    /**
     * Registers a code-based disc together with a custom {@link Item} subclass.
     * <p>
     * Identical to
     * {@link #createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)} but
     * the item is constructed by the caller-supplied {@code factory}, allowing
     * arbitrary {@link Item} subclasses (e.g. an item with custom tooltip lines
     * or use behaviour).
     *
     * @param register   the mod's item {@link DeferredRegister.Items}; must not
     *                   be {@code null} and must already be attached to the mod
     *                   event bus
     * @param id         the disc id, also used as the item id
     * @param properties base item properties; disc-related components are added
     *                   before {@code factory} is invoked
     * @param factory    constructor reference for the {@link Item} subclass,
     *                   e.g. {@code MyDiscItem::new}
     * @param disc       the disc data
     * @param <T>        the {@link Item} subclass returned by {@code factory}
     * @return a {@code DeferredItem<T>} pointing at the deferred item,
     * statically typed to {@code T}
     * @since 0.1.3
     */
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

    /**
     * Returns an unmodifiable-style view of every disc registered via
     * {@link #createDisc(Identifier, BackstubeMusicDisc)} since startup.
     * <p>
     * Intended primarily for Backstube's own internal pipelines (resource pack
     * injection, jukebox song synthesis). External consumers should not rely on
     * the map for state checks &mdash; query the live registry through a
     * {@link HolderLookup.Provider} instead.
     *
     * @return the map of code-registered discs; insertion-ordered
     * @since 0.1.3
     */
    public static Map<Identifier, BackstubeMusicDisc> codeDiscs() {
        return CODE_DISCS;
    }

    /**
     * Merges code-registered discs into a resource-listing map produced by a
     * {@link FileToIdConverter}, returning a new map.
     * <p>
     * Internal hook used by Backstube's registry-loader mixin to make Java-only
     * discs visible to vanilla's registry loading code. Synthesized JSON
     * resources are added only for ids that are not already present in
     * {@code original} (i.e. JSON files win over code).
     *
     * @param converter the converter that produced {@code original}; defines the
     *                  file-id translation used for synthesized entries
     * @param original  the original mapping; left untouched if there are no
     *                  code-registered discs or no source pack to attribute the
     *                  synthesized resources to
     * @return either {@code original} unchanged, or a fresh map containing every
     * original entry plus synthesized entries for code-registered discs
     * @since 0.1.3
     */
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
            merged.put(fileId, new Resource(source, supplier) {
                @Override
                public Optional<KnownPack> knownPackInfo() {
                    return Optional.empty();
                }
            });
        }
        return merged;
    }

    /**
     * Builds a fresh {@link ItemStack} representing the given disc, ready to be
     * inserted into an inventory.
     * <p>
     * The {@link Item} of the stack is chosen as follows:
     * <ol>
     *   <li>If the disc's {@link BackstubeMusicDisc#item()} is present and
     *       resolves in {@link BuiltInRegistries#ITEM}, that item is used.</li>
     *   <li>Otherwise the generic {@code backstube:music_disc} item is used.</li>
     * </ol>
     * The returned stack already has the {@code backstube:disc},
     * {@code minecraft:rarity}, optionally {@code minecraft:max_stack_size} and
     * {@code minecraft:jukebox_playable} components set via
     * {@link #applyDiscData(ItemStack, Holder, HolderLookup.Provider)}.
     *
     * @param disc     the resolved disc holder; must carry a registry key (i.e.
     *                 must originate from a registry lookup, not
     *                 {@link Holder#direct(Object)})
     * @param provider a {@link HolderLookup.Provider}; obtainable from
     *                 {@code Level#registryAccess()} or
     *                 {@code RegistryAccess}
     * @return a stack of size 1; never {@code null}
     * @throws java.util.NoSuchElementException if {@code disc} has no registry key
     * @since 0.1.2
     */
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

    /**
     * Convenience overload that resolves {@code diskKey} through {@code provider}
     * and builds the corresponding stack.
     * <p>
     * Returns {@link Optional#empty()} if the disc is not present in the
     * registry seen by {@code provider} (e.g. the datapack defining the disc was
     * not loaded).
     *
     * @param diskKey  the disc registry key
     * @param provider a {@link HolderLookup.Provider}
     * @return the disc stack, or {@link Optional#empty()} if {@code diskKey} is
     * unknown to the registry
     * @see #discStack(Holder, HolderLookup.Provider)
     * @since 0.1.2
     */
    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(holder -> discStack(holder, provider));
    }

    /**
     * Stamps the disc-related data components on an existing stack in place.
     * <p>
     * Sets:
     * <ul>
     *   <li>{@code backstube:disc} &rarr; {@code disc};</li>
     *   <li>{@code minecraft:rarity} &rarr; {@link BackstubeMusicDisc#rarity()};</li>
     *   <li>{@code minecraft:max_stack_size} &rarr;
     *       {@link BackstubeMusicDisc#stackSize()} (only when the disc's stack
     *       size differs from the vanilla item default of 64);</li>
     *   <li>{@code minecraft:jukebox_playable} &rarr; the
     *       {@link net.minecraft.world.item.JukeboxPlayable} matching the disc
     *       id, when one is available in the registry seen by
     *       {@code provider}.</li>
     * </ul>
     * Useful when starting from an arbitrary stack (for example, one created by
     * a recipe or a creative-tab generator) and turning it into a Backstube
     * disc. For a fresh stack prefer
     * {@link #discStack(Holder, HolderLookup.Provider)}.
     *
     * @param stack    the stack to mutate; must not be {@code null}
     * @param disc     the disc holder; must carry a registry key
     * @param provider a {@link HolderLookup.Provider} used to look up the
     *                 matching jukebox song
     * @throws java.util.NoSuchElementException if {@code disc} has no registry key
     * @since 0.1.3
     */
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

    /**
     * Resolves {@code diskKey} through {@code provider} and stamps the data on
     * {@code stack}, returning whether the disc was found.
     * <p>
     * Equivalent to looking the key up first and then calling
     * {@link #applyDiscData(ItemStack, Holder, HolderLookup.Provider)}, but
     * fails silently and reports the result via the return value when the disc
     * is unknown.
     *
     * @param stack    the stack to mutate; left untouched if {@code diskKey} is
     *                 not registered
     * @param diskKey  the disc registry key
     * @param provider a {@link HolderLookup.Provider}
     * @return {@code true} if a matching disc was found and applied,
     * {@code false} otherwise
     * @since 0.1.3
     */
    public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        Optional<Holder.Reference<BackstubeMusicDisc>> opt = provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey));
        opt.ifPresent(holder -> applyDiscData(stack, holder, provider));
        return opt.isPresent();
    }

    /**
     * Returns whether the given stack carries Backstube disc data.
     * <p>
     * Equivalent to checking for the presence of the
     * {@linkplain #discComponent() backstube:disc} component on the stack.
     *
     * @param stack the stack to test; passing an empty stack returns
     *              {@code false}
     * @return {@code true} when the stack has the {@code backstube:disc}
     * component
     * @since 0.1.3
     */
    public static boolean isDisc(ItemStack stack) {
        return stack.has(ModComponents.DISC);
    }

    /**
     * Returns the disc carried by the given stack, if any.
     * <p>
     * Wraps the stack's {@code backstube:disc} component lookup in an
     * {@link Optional}; empty when the component is absent.
     *
     * @param stack the stack to inspect; passing an empty stack returns
     *              {@link Optional#empty()}
     * @return the disc holder, or {@link Optional#empty()} when the stack is
     * not a Backstube disc
     * @since 0.1.2
     */
    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.DISC));
    }

    /**
     * Streams every disc registered in the {@code backstube:music_disc} registry
     * visible to {@code provider}.
     * <p>
     * The order matches the registry's natural iteration order. Returns an
     * empty stream if the registry is not available (for example, before the
     * world is loaded).
     *
     * @param provider a {@link HolderLookup.Provider}; obtainable from
     *                 {@code Level#registryAccess()} or any
     *                 {@link net.minecraft.core.RegistryAccess} instance
     * @return a non-{@code null} stream of disc holders
     * @since 0.1.2
     */
    public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .map(HolderLookup.RegistryLookup::listElements)
                .orElseGet(Stream::empty);
    }
}
