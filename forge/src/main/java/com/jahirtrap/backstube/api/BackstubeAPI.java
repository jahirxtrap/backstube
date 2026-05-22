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

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

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
 * {@link RecordItem} whose per-stack disc identity lives in the
 * {@code BackstubeDisc} NBT tag. Mods, datapacks and resource packs add new
 * discs by registering a {@link BackstubeMusicDisc} under a unique id; the
 * disc's audio file is auto-resolved from that id.
 * <p>
 * Discs can be added in three ways:
 * <ul>
 *   <li><b>Data-driven</b> &mdash; ship a JSON file under
 *       {@code data/<namespace>/backstube/music_disc/<path>.json} plus an OGG
 *       Vorbis mono audio asset; no Java code required.</li>
 *   <li><b>Pure Java</b> &mdash; register everything in code via
 *       {@link #createDisc(ResourceLocation, BackstubeMusicDisc)} or one of its
 *       item-aware overloads.</li>
 *   <li><b>Hybrid</b> &mdash; register a custom item with
 *       {@link #discProperties()} and provide the disc data in JSON,
 *       optionally pointing back to the item via the {@code item} field.</li>
 * </ul>
 * <p>
 * All methods are static; this class is not instantiable.
 *
 * @see BackstubeMusicDisc
 * @see DiscSound
 * @since 0.1.2
 */
public class BackstubeAPI {
    private static final String DISC_KEY = "BackstubeDisc";
    private static final String RARITY_KEY = "BackstubeRarity";
    private static final String DESCRIPTION_KEY = "BackstubeDescription";
    private static final String STACK_SIZE_KEY = "BackstubeStackSize";

    private static final Map<ResourceLocation, BackstubeMusicDisc> CODE_DISCS = new LinkedHashMap<>();

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
     * Builds {@link Item.Properties} pre-configured for a {@link RecordItem}
     * music disc.
     * <p>
     * The returned properties have:
     * <ul>
     *   <li>{@code stacksTo(1)}.</li>
     * </ul>
     * <p>
     * In MC 1.20.1 vanilla discs are bound to their {@link net.minecraft.sounds.SoundEvent}
     * through the {@link RecordItem} constructor (not through a {@code jukeboxPlayable}
     * component); the binding therefore happens at item construction time, not
     * via these properties. The disc data (title, artist, length, rarity, etc.)
     * is stamped on the stack at creation time by
     * {@link #applyDiscData(ItemStack, Holder)} (the helper invoked by
     * {@link #discStack(Holder)}). The disc itself must still exist either as a
     * JSON file or as a code-registered entry via
     * {@link #createDisc(ResourceLocation, BackstubeMusicDisc)}.
     *
     * @return a fresh {@link Item.Properties} ready to be passed to a
     *         {@link RecordItem} constructor; never {@code null}
     * @see #createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)
     * @since 0.1.2
     */
    public static Item.Properties discProperties() {
        return new Item.Properties().stacksTo(1);
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
     * @param id   the disc id, e.g. {@code ResourceLocation.fromNamespaceAndPath("example", "cool_song")};
     *             must not be {@code null}
     * @param disc the disc data, typically built with
     *             {@link BackstubeMusicDisc#builder()}; must not be {@code null}
     * @see #createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)
     * @since 0.1.3
     */
    public static void createDisc(ResourceLocation id, BackstubeMusicDisc disc) {
        BackstubeMusicDisc effective = disc.item().isPresent() ? disc
                : new BackstubeMusicDisc(disc.title(), disc.artist(), disc.lengthInSeconds(),
                disc.comparatorOutput(), disc.rarity(), disc.model(), disc.sound(),
                disc.stackSize(), Optional.of(id));
        CODE_DISCS.put(id, effective);
    }

    /**
     * Registers a code-based disc together with a {@link RecordItem} bound to
     * its registry entry.
     * <p>
     * Equivalent to calling {@link #createDisc(ResourceLocation, BackstubeMusicDisc)}
     * and then registering a {@link RecordItem} under {@code id} with the
     * supplied {@code properties}. The {@link net.minecraft.sounds.SoundEvent}
     * of the record is the shared {@code backstube:disc} event; the per-disc
     * audio file is resolved at playback time from the disc id.
     * <p>
     * The item is deferred through the caller-provided {@link DeferredRegister}
     * and constructed lazily when the Forge registry event fires; assign the
     * returned {@link RegistryObject} to a {@code static final} field so static
     * initialization order is preserved.
     *
     * @param register   the mod's item {@link DeferredRegister}; must not be
     *                   {@code null} and must already be attached to the mod
     *                   event bus
     * @param id         the disc id, also used as the item id
     * @param properties base item properties; {@code stacksTo} is applied
     *                   automatically
     * @param disc       the disc data
     * @return a {@code RegistryObject<RecordItem>} pointing at the deferred
     *         item; never {@code null}
     * @see #createDisc(DeferredRegister, ResourceLocation, Item.Properties, Function, BackstubeMusicDisc)
     * @since 0.1.3
     */
    public static RegistryObject<RecordItem> createDisc(DeferredRegister<Item> register, ResourceLocation id, Item.Properties properties, BackstubeMusicDisc disc) {
        return createDisc(register, id, properties, props -> new RecordItem(1, ModContent.SOUND_DISC.get(), props, 0), disc);
    }

    /**
     * Registers a code-based disc together with a custom {@link RecordItem}
     * subclass.
     * <p>
     * Identical to
     * {@link #createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)} but
     * the item is constructed by the caller-supplied {@code factory}, allowing
     * arbitrary {@link RecordItem} subclasses (e.g. an item with custom tooltip
     * lines or use behaviour).
     *
     * @param register   the mod's item {@link DeferredRegister}; must not be
     *                   {@code null} and must already be attached to the mod
     *                   event bus
     * @param id         the disc id, also used as the item id
     * @param properties base item properties
     * @param factory    constructor reference for the {@link RecordItem}
     *                   subclass, e.g. {@code MyDiscItem::new}
     * @param disc       the disc data
     * @param <T>        the {@link RecordItem} subclass returned by
     *                   {@code factory}
     * @return a {@code RegistryObject<T>} pointing at the deferred item,
     *         statically typed to {@code T}
     * @since 0.1.3
     */
    public static <T extends RecordItem> RegistryObject<T> createDisc(DeferredRegister<Item> register, ResourceLocation id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc) {
        return register.register(id.getPath(), () -> {
            createDisc(id, disc);
            return factory.apply(properties);
        });
    }

    /**
     * Returns an unmodifiable-style view of every disc registered via
     * {@link #createDisc(ResourceLocation, BackstubeMusicDisc)} since startup.
     * <p>
     * Intended primarily for Backstube's own internal pipelines (resource pack
     * injection, jukebox song synthesis). External consumers should not rely on
     * the map for state checks &mdash; query the live registry through a
     * {@link HolderLookup.Provider} instead.
     *
     * @return the map of code-registered discs; insertion-ordered
     * @since 0.1.3
     */
    public static Map<ResourceLocation, BackstubeMusicDisc> codeDiscs() {
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
     *         original entry plus synthesized entries for code-registered discs
     * @since 0.1.3
     */
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
     * The returned stack already has the {@code BackstubeDisc},
     * {@code BackstubeRarity}, {@code BackstubeDescription} and
     * {@code BackstubeStackSize} NBT tags set via
     * {@link #applyDiscData(ItemStack, Holder)}.
     *
     * @param disc the resolved disc holder; must carry a registry key (i.e. must
     *             originate from a registry lookup, not
     *             {@link Holder#direct(Object)})
     * @return a stack of size 1; never {@code null}
     * @throws java.util.NoSuchElementException if {@code disc} has no registry key
     * @since 0.1.2
     */
    public static ItemStack discStack(Holder<BackstubeMusicDisc> disc) {
        BackstubeMusicDisc value = disc.value();
        Item item = value.item()
                .filter(BuiltInRegistries.ITEM::containsKey)
                .map(BuiltInRegistries.ITEM::get)
                .orElse(ModContent.MUSIC_DISC.get());
        ItemStack stack = new ItemStack(item);
        applyDiscData(stack, disc);
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
     *         unknown to the registry
     * @see #discStack(Holder)
     * @since 0.1.2
     */
    public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        return provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey))
                .map(BackstubeAPI::discStack);
    }

    /**
     * Stamps the disc-related NBT tags on an existing stack in place.
     * <p>
     * Sets on {@code stack.getOrCreateTag()}:
     * <ul>
     *   <li>{@code BackstubeDisc} (string) &rarr; the disc id;</li>
     *   <li>{@code BackstubeRarity} (string) &rarr;
     *       {@link BackstubeMusicDisc#rarity()}{@code .name()};</li>
     *   <li>{@code BackstubeDescription} (string) &rarr; the JSON-serialised
     *       {@link BackstubeMusicDisc#description()};</li>
     *   <li>{@code BackstubeStackSize} (int) &rarr;
     *       {@link BackstubeMusicDisc#stackSize()}.</li>
     * </ul>
     * Useful when starting from an arbitrary stack (for example, one created by
     * a recipe or a creative-tab generator) and turning it into a Backstube
     * disc. For a fresh stack prefer {@link #discStack(Holder)}.
     *
     * @param stack the stack to mutate; must not be {@code null}
     * @param disc  the disc holder; must carry a registry key
     * @throws java.util.NoSuchElementException if {@code disc} has no registry key
     * @since 0.1.3
     */
    public static void applyDiscData(ItemStack stack, Holder<BackstubeMusicDisc> disc) {
        BackstubeMusicDisc value = disc.value();
        ResourceLocation id = disc.unwrapKey().orElseThrow().location();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(DISC_KEY, id.toString());
        tag.putString(RARITY_KEY, value.rarity().name());
        tag.putString(DESCRIPTION_KEY, Component.Serializer.toJson(value.description()));
        tag.putInt(STACK_SIZE_KEY, value.stackSize());
    }

    /**
     * Reads the disc's max stack size cached on the stack's NBT.
     * <p>
     * Returns the value previously written to {@code BackstubeStackSize} by
     * {@link #applyDiscData(ItemStack, Holder)}, or {@code -1} when the tag is
     * absent. In MC 1.20.1 vanilla items cannot expose a per-stack max stack
     * size, so Backstube's {@link ItemStack} mixin reads this value to override
     * {@link ItemStack#getMaxStackSize()} on a per-stack basis.
     *
     * @param stack the stack to inspect
     * @return the disc's stack size, or {@code -1} if the tag is not set
     * @since 0.1.3
     */
    public static int readStackSize(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(STACK_SIZE_KEY, Tag.TAG_INT)) return -1;
        return tag.getInt(STACK_SIZE_KEY);
    }

    /**
     * Resolves {@code diskKey} through {@code provider} and stamps the data on
     * {@code stack}, returning whether the disc was found.
     * <p>
     * Equivalent to looking the key up first and then calling
     * {@link #applyDiscData(ItemStack, Holder)}, but fails silently and reports
     * the result via the return value when the disc is unknown.
     *
     * @param stack    the stack to mutate; left untouched if {@code diskKey} is
     *                 not registered
     * @param diskKey  the disc registry key
     * @param provider a {@link HolderLookup.Provider}
     * @return {@code true} if a matching disc was found and applied,
     *         {@code false} otherwise
     * @since 0.1.3
     */
    public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider) {
        Optional<Holder.Reference<BackstubeMusicDisc>> opt = provider.lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(diskKey));
        opt.ifPresent(holder -> applyDiscData(stack, holder));
        return opt.isPresent();
    }

    /**
     * Returns whether the given stack carries Backstube disc data.
     * <p>
     * Equivalent to checking for the presence of the {@code BackstubeDisc}
     * string NBT tag on the stack.
     *
     * @param stack the stack to test; passing an empty stack returns
     *              {@code false}
     * @return {@code true} when the stack has the {@code BackstubeDisc} NBT tag
     * @since 0.1.3
     */
    public static boolean isDisc(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(DISC_KEY, Tag.TAG_STRING);
    }

    /**
     * Returns the disc carried by the given stack, if any, resolved against the
     * given {@link Level}'s registry access.
     * <p>
     * Convenience overload that calls {@link #readDisc(ItemStack, HolderLookup.Provider)}
     * with {@code level.registryAccess()}. Empty when the {@code BackstubeDisc}
     * NBT tag is absent or its id is unknown to the registry.
     *
     * @param stack the stack to inspect; passing an empty stack returns
     *              {@link Optional#empty()}
     * @param level the level whose registry access is used to resolve the disc
     * @return the disc holder, or {@link Optional#empty()} when the stack is
     *         not a Backstube disc or the disc is missing from the registry
     * @see #readDisc(ItemStack, HolderLookup.Provider)
     * @since 0.1.2
     */
    public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, Level level) {
        return readDisc(stack, level.registryAccess());
    }

    /**
     * Returns the disc carried by the given stack, if any.
     * <p>
     * Reads the {@code BackstubeDisc} NBT tag from {@code stack} and resolves
     * it against the {@code backstube:music_disc} registry visible to
     * {@code provider}. Empty when the tag is absent or the disc id is unknown
     * to the registry.
     *
     * @param stack    the stack to inspect; passing an empty stack returns
     *                 {@link Optional#empty()}
     * @param provider a {@link HolderLookup.Provider}; obtainable from
     *                 {@code Level#registryAccess()} or any
     *                 {@link net.minecraft.core.RegistryAccess} instance
     * @return the disc holder, or {@link Optional#empty()} when the stack is
     *         not a Backstube disc or the disc is missing from the registry
     * @since 0.1.2
     */
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

    /**
     * Reads the cached "Artist - Title" component from the stack's NBT.
     * <p>
     * Returns the value previously written to {@code BackstubeDescription} by
     * {@link #applyDiscData(ItemStack, Holder)}, deserialised from JSON, or
     * {@code null} when the tag is absent. Used by Backstube's
     * {@link RecordItem} mixin to override {@link RecordItem#getDisplayName()}
     * so tooltips and the {@code Now Playing} message show the disc's
     * description even when the registry is not available on the client.
     *
     * @param stack the stack to inspect
     * @return the cached description {@link Component}, or {@code null} if the
     *         tag is not set
     * @since 0.1.3
     */
    public static Component readDescription(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DESCRIPTION_KEY, Tag.TAG_STRING)) return null;
        return Component.Serializer.fromJson(tag.getString(DESCRIPTION_KEY));
    }

    /**
     * Reads the disc's {@link Rarity} from the stack's NBT.
     * <p>
     * Returns the value previously written to {@code BackstubeRarity} by
     * {@link #applyDiscData(ItemStack, Holder)}, parsed by
     * {@link Rarity#valueOf(String)}. Falls back to {@link Rarity#COMMON} when
     * the tag is absent or holds an unknown value. Used by Backstube's
     * {@link Item} mixin to override {@link Item#getRarity(ItemStack)} on a
     * per-stack basis.
     *
     * @param stack the stack to inspect
     * @return the disc's {@link Rarity}; never {@code null}
     * @since 0.1.3
     */
    public static Rarity readRarity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(RARITY_KEY, Tag.TAG_STRING)) return Rarity.COMMON;
        try {
            return Rarity.valueOf(tag.getString(RARITY_KEY));
        } catch (IllegalArgumentException e) {
            return Rarity.COMMON;
        }
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

    /**
     * Resolves the audio file id that should play for the given disc.
     * <p>
     * Equivalent to
     * {@code disc.sound().orElse(DiscSound.DEFAULT).resolveName(discId)}: if the
     * disc supplies a {@link DiscSound} with an explicit {@code name}, that
     * value is returned verbatim; otherwise the id is derived from
     * {@code discId} by prefixing the path with {@code records/} (see
     * {@link DiscSound#resolveName(ResourceLocation)}).
     *
     * @param disc   the disc data; must not be {@code null}
     * @param discId the disc id; must not be {@code null}
     * @return the resolved sound id; never {@code null}
     * @see DiscSound#resolveName(ResourceLocation)
     * @since 0.1.2
     */
    public static ResourceLocation discSoundLocation(BackstubeMusicDisc disc, ResourceLocation discId) {
        DiscSound config = disc.sound().orElse(DiscSound.DEFAULT);
        return config.resolveName(discId);
    }
}
