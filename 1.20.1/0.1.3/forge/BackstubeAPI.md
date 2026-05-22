# Class `BackstubeAPI`

**Package:** `com.jahirtrap.backstube.api`

```java
public class BackstubeAPI
```

Public entry point for the Backstube music disc framework.

Backstube unifies every music disc into a single `backstube:music_disc`
`RecordItem` whose per-stack disc identity lives in the
`BackstubeDisc` NBT tag. Mods, datapacks and resource packs add new
discs by registering a [`BackstubeMusicDisc`](BackstubeMusicDisc.md) under a unique id; the
disc's audio file is auto-resolved from that id.

Discs can be added in three ways:

- **Data-driven** — ship a JSON file under
`data/<namespace>/backstube/music_disc/<path>.json` plus an OGG
Vorbis mono audio asset; no Java code required.

- **Pure Java** — register everything in code via
[`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc) or one of its
item-aware overloads.

- **Hybrid** — register a custom item with
[`discProperties()`](#discproperties) and provide the disc data in JSON,
optionally pointing back to the item via the `item` field.

All methods are static; this class is not instantiable.

> **See also**
> 
> - [`BackstubeMusicDisc`](BackstubeMusicDisc.md)
> - [`DiscSound`](DiscSound.md)
> 
> **Since** 0.1.2

---

## Methods

| Method | Summary |
|---|---|
| [`discRegistryKey()`](#discregistrykey) | Returns the registry key for the `backstube:music_disc` registry. |
| [`discProperties()`](#discproperties) | Builds `Item.Properties` pre-configured for a `RecordItem` music disc. |
| [`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc) | Registers a code-based disc backed by the generic `backstube:music_disc` item. |
| [`createDisc(DeferredRegister<Item>, ResourceLocation, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-item-resourcelocation-item-properties-backstubemusicdisc) | Registers a code-based disc together with a `RecordItem` bound to its registry entry. |
| [`createDisc(DeferredRegister<Item>, ResourceLocation, Item.Properties, Function<Item.Properties, T>, BackstubeMusicDisc)`](#createdisc-deferredregister-item-resourcelocation-item-properties-function-item-properties-t-backstubemusicdisc) | Registers a code-based disc together with a custom `RecordItem` subclass. |
| [`codeDiscs()`](#codediscs) | Returns an unmodifiable-style view of every disc registered via [`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc) since startup. |
| [`injectCodeDiscs(FileToIdConverter, Map<ResourceLocation, Resource>)`](#injectcodediscs-filetoidconverter-map-resourcelocation-resource) | Merges code-registered discs into a resource-listing map produced by a `FileToIdConverter`, returning a new map. |
| [`discStack(Holder<BackstubeMusicDisc>)`](#discstack-holder-backstubemusicdisc) | Builds a fresh `ItemStack` representing the given disc, ready to be inserted into an inventory. |
| [`discStack(ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`](#discstack-resourcekey-backstubemusicdisc-holderlookup-provider) | Convenience overload that resolves `diskKey` through `provider` and builds the corresponding stack. |
| [`applyDiscData(ItemStack, Holder<BackstubeMusicDisc>)`](#applydiscdata-itemstack-holder-backstubemusicdisc) | Stamps the disc-related NBT tags on an existing stack in place. |
| [`readStackSize(ItemStack)`](#readstacksize-itemstack) | Reads the disc's max stack size cached on the stack's NBT. |
| [`applyDiscData(ItemStack, ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`](#applydiscdata-itemstack-resourcekey-backstubemusicdisc-holderlookup-provider) | Resolves `diskKey` through `provider` and stamps the data on `stack`, returning whether the disc was found. |
| [`isDisc(ItemStack)`](#isdisc-itemstack) | Returns whether the given stack carries Backstube disc data. |
| [`readDisc(ItemStack, Level)`](#readdisc-itemstack-level) | Returns the disc carried by the given stack, if any, resolved against the given `Level`'s registry access. |
| [`readDisc(ItemStack, HolderLookup.Provider)`](#readdisc-itemstack-holderlookup-provider) | Returns the disc carried by the given stack, if any. |
| [`readDescription(ItemStack)`](#readdescription-itemstack) | Reads the cached "Artist - Title" component from the stack's NBT. |
| [`readRarity(ItemStack)`](#readrarity-itemstack) | Reads the disc's `Rarity` from the stack's NBT. |
| [`getAllDiscs(HolderLookup.Provider)`](#getalldiscs-holderlookup-provider) | Streams every disc registered in the `backstube:music_disc` registry visible to `provider`. |
| [`discSoundLocation(BackstubeMusicDisc, ResourceLocation)`](#discsoundlocation-backstubemusicdisc-resourcelocation) | Resolves the audio file id that should play for the given disc. |

---

### `discRegistryKey()`

```java
public static ResourceKey<Registry<BackstubeMusicDisc>> discRegistryKey()
```

Returns the registry key for the `backstube:music_disc` registry.

Use this to declare disc-related `ResourceKey` instances, register
tags under `data/<namespace>/tags/backstube/music_disc/` or hook datapack
registry handlers.

**Returns:** the registry key; never `null`

> **See also:** [`BackstubeMusicDisc.REGISTRY_KEY`](BackstubeMusicDisc.md#registry_key)
> 
> **Since** 0.1.2

---

### `discProperties()`

```java
public static Item.Properties discProperties()
```

Builds `Item.Properties` pre-configured for a `RecordItem`
music disc.

The returned properties have:

- `stacksTo(1)`.

In MC 1.20.1 vanilla discs are bound to their `net.minecraft.sounds.SoundEvent`
through the `RecordItem` constructor (not through a `jukeboxPlayable`
component); the binding therefore happens at item construction time, not
via these properties. The disc data (title, artist, length, rarity, etc.)
is stamped on the stack at creation time by
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder) (the helper invoked by
[`discStack(Holder)`](#discstack-holder)). The disc itself must still exist either as a
JSON file or as a code-registered entry via
[`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc).

**Returns:** a fresh `Item.Properties` ready to be passed to a
`RecordItem` constructor; never `null`

> **See also:** [`createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-resourcelocation-item-properties-backstubemusicdisc)
> 
> **Since** 0.1.2

---

### `createDisc(ResourceLocation, BackstubeMusicDisc)`

```java
public static void createDisc(ResourceLocation id, BackstubeMusicDisc disc)
```

Registers a code-based disc backed by the generic `backstube:music_disc`
item.

The disc is added to an in-memory map and merged into the
`backstube:music_disc` data registry at datapack load time. If a JSON
file with the same id exists, the JSON entry takes precedence over the
code-registered one.

The supplied disc is rewritten so its [`BackstubeMusicDisc.item()`](BackstubeMusicDisc.md#item)
field always equals `id`; any user-supplied value of `item` on
the input is ignored.

**Parameters:**

| Name | Description |
|---|---|
| `id` | the disc id, e.g. `ResourceLocation.fromNamespaceAndPath("example", "cool_song")`; must not be `null` |
| `disc` | the disc data, typically built with [`BackstubeMusicDisc.builder()`](BackstubeMusicDisc.md#builder); must not be `null` |

> **See also:** [`createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-resourcelocation-item-properties-backstubemusicdisc)
> 
> **Since** 0.1.3

---

### `createDisc(DeferredRegister<Item>, ResourceLocation, Item.Properties, BackstubeMusicDisc)`

```java
public static RegistryObject<RecordItem> createDisc(DeferredRegister<Item> register, ResourceLocation id, Item.Properties properties, BackstubeMusicDisc disc)
```

Registers a code-based disc together with a `RecordItem` bound to
its registry entry.

Equivalent to calling [`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc)
and then registering a `RecordItem` under `id` with the
supplied `properties`. The `net.minecraft.sounds.SoundEvent`
of the record is the shared `backstube:disc` event; the per-disc
audio file is resolved at playback time from the disc id.

The item is deferred through the caller-provided `DeferredRegister`
and constructed lazily when the Forge registry event fires; assign the
returned `RegistryObject` to a `static final` field so static
initialization order is preserved.

**Parameters:**

| Name | Description |
|---|---|
| `register` | the mod's item `DeferredRegister`; must not be `null` and must already be attached to the mod event bus |
| `id` | the disc id, also used as the item id |
| `properties` | base item properties; `stacksTo` is applied automatically |
| `disc` | the disc data |

**Returns:** a `RegistryObject<RecordItem>` pointing at the deferred
item; never `null`

> **See also:** [`createDisc(DeferredRegister, ResourceLocation, Item.Properties, Function, BackstubeMusicDisc)`](#createdisc-deferredregister-resourcelocation-item-properties-function-backstubemusicdisc)
> 
> **Since** 0.1.3

---

### `createDisc(DeferredRegister<Item>, ResourceLocation, Item.Properties, Function<Item.Properties, T>, BackstubeMusicDisc)`

```java
public static <T extends RecordItem> RegistryObject<T> createDisc(DeferredRegister<Item> register, ResourceLocation id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc)
```

Registers a code-based disc together with a custom `RecordItem`
subclass.

Identical to
[`createDisc(DeferredRegister, ResourceLocation, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-resourcelocation-item-properties-backstubemusicdisc) but
the item is constructed by the caller-supplied `factory`, allowing
arbitrary `RecordItem` subclasses (e.g. an item with custom tooltip
lines or use behaviour).

**Parameters:**

| Name | Description |
|---|---|
| `register` | the mod's item `DeferredRegister`; must not be `null` and must already be attached to the mod event bus |
| `id` | the disc id, also used as the item id |
| `properties` | base item properties |
| `factory` | constructor reference for the `RecordItem` subclass, e.g. `MyDiscItem::new` |
| `disc` | the disc data |

**Returns:** a `RegistryObject<T>` pointing at the deferred item,
statically typed to `T`

> **Since** 0.1.3

---

### `codeDiscs()`

```java
public static Map<ResourceLocation, BackstubeMusicDisc> codeDiscs()
```

Returns an unmodifiable-style view of every disc registered via
[`createDisc(ResourceLocation, BackstubeMusicDisc)`](#createdisc-resourcelocation-backstubemusicdisc) since startup.

Intended primarily for Backstube's own internal pipelines (resource pack
injection, jukebox song synthesis). External consumers should not rely on
the map for state checks — query the live registry through a
`HolderLookup.Provider` instead.

**Returns:** the map of code-registered discs; insertion-ordered

> **Since** 0.1.3

---

### `injectCodeDiscs(FileToIdConverter, Map<ResourceLocation, Resource>)`

```java
public static Map<ResourceLocation, Resource> injectCodeDiscs(FileToIdConverter converter, Map<ResourceLocation, Resource> original)
```

Merges code-registered discs into a resource-listing map produced by a
`FileToIdConverter`, returning a new map.

Internal hook used by Backstube's registry-loader mixin to make Java-only
discs visible to vanilla's registry loading code. Synthesized JSON
resources are added only for ids that are not already present in
`original` (i.e. JSON files win over code).

**Parameters:**

| Name | Description |
|---|---|
| `converter` | the converter that produced `original`; defines the file-id translation used for synthesized entries |
| `original` | the original mapping; left untouched if there are no code-registered discs or no source pack to attribute the synthesized resources to |

**Returns:** either `original` unchanged, or a fresh map containing every
original entry plus synthesized entries for code-registered discs

> **Since** 0.1.3

---

### `discStack(Holder<BackstubeMusicDisc>)`

```java
public static ItemStack discStack(Holder<BackstubeMusicDisc> disc)
```

Builds a fresh `ItemStack` representing the given disc, ready to be
inserted into an inventory.

The `Item` of the stack is chosen as follows:

1. If the disc's [`BackstubeMusicDisc.item()`](BackstubeMusicDisc.md#item) is present and
resolves in `BuiltInRegistries.ITEM`, that item is used.

2. Otherwise the generic `backstube:music_disc` item is used.

The returned stack already has the `BackstubeDisc`,
`BackstubeRarity`, `BackstubeDescription` and
`BackstubeStackSize` NBT tags set via
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder).

**Parameters:**

| Name | Description |
|---|---|
| `disc` | the resolved disc holder; must carry a registry key (i.e. must originate from a registry lookup, not `Holder.direct(Object)`) |

**Returns:** a stack of size 1; never `null`

**Throws:**

| Type | Condition |
|---|---|
| `java.util.NoSuchElementException` | if `disc` has no registry key |

> **Since** 0.1.2

---

### `discStack(ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`

```java
public static Optional<ItemStack> discStack(ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider)
```

Convenience overload that resolves `diskKey` through `provider`
and builds the corresponding stack.

Returns `Optional.empty()` if the disc is not present in the
registry seen by `provider` (e.g. the datapack defining the disc was
not loaded).

**Parameters:**

| Name | Description |
|---|---|
| `diskKey` | the disc registry key |
| `provider` | a `HolderLookup.Provider` |

**Returns:** the disc stack, or `Optional.empty()` if `diskKey` is
unknown to the registry

> **See also:** [`discStack(Holder)`](#discstack-holder)
> 
> **Since** 0.1.2

---

### `applyDiscData(ItemStack, Holder<BackstubeMusicDisc>)`

```java
public static void applyDiscData(ItemStack stack, Holder<BackstubeMusicDisc> disc)
```

Stamps the disc-related NBT tags on an existing stack in place.

Sets on `stack.getOrCreateTag()`:

- `BackstubeDisc` (string) → the disc id;

- `BackstubeRarity` (string) →
[`BackstubeMusicDisc.rarity()`](BackstubeMusicDisc.md#rarity)`.name()`;

- `BackstubeDescription` (string) → the JSON-serialised
[`BackstubeMusicDisc.description()`](BackstubeMusicDisc.md#description);

- `BackstubeStackSize` (int) →
[`BackstubeMusicDisc.stackSize()`](BackstubeMusicDisc.md#stacksize).

Useful when starting from an arbitrary stack (for example, one created by
a recipe or a creative-tab generator) and turning it into a Backstube
disc. For a fresh stack prefer [`discStack(Holder)`](#discstack-holder).

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to mutate; must not be `null` |
| `disc` | the disc holder; must carry a registry key |

**Throws:**

| Type | Condition |
|---|---|
| `java.util.NoSuchElementException` | if `disc` has no registry key |

> **Since** 0.1.3

---

### `readStackSize(ItemStack)`

```java
public static int readStackSize(ItemStack stack)
```

Reads the disc's max stack size cached on the stack's NBT.

Returns the value previously written to `BackstubeStackSize` by
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder), or `-1` when the tag is
absent. In MC 1.20.1 vanilla items cannot expose a per-stack max stack
size, so Backstube's `ItemStack` mixin reads this value to override
`ItemStack.getMaxStackSize()` on a per-stack basis.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect |

**Returns:** the disc's stack size, or `-1` if the tag is not set

> **Since** 0.1.3

---

### `applyDiscData(ItemStack, ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`

```java
public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider)
```

Resolves `diskKey` through `provider` and stamps the data on
`stack`, returning whether the disc was found.

Equivalent to looking the key up first and then calling
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder), but fails silently and reports
the result via the return value when the disc is unknown.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to mutate; left untouched if `diskKey` is not registered |
| `diskKey` | the disc registry key |
| `provider` | a `HolderLookup.Provider` |

**Returns:** `true` if a matching disc was found and applied,
`false` otherwise

> **Since** 0.1.3

---

### `isDisc(ItemStack)`

```java
public static boolean isDisc(ItemStack stack)
```

Returns whether the given stack carries Backstube disc data.

Equivalent to checking for the presence of the `BackstubeDisc`
string NBT tag on the stack.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to test; passing an empty stack returns `false` |

**Returns:** `true` when the stack has the `BackstubeDisc` NBT tag

> **Since** 0.1.3

---

### `readDisc(ItemStack, Level)`

```java
public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, Level level)
```

Returns the disc carried by the given stack, if any, resolved against the
given `Level`'s registry access.

Convenience overload that calls [`readDisc(ItemStack, HolderLookup.Provider)`](#readdisc-itemstack-holderlookup-provider)
with `level.registryAccess()`. Empty when the `BackstubeDisc`
NBT tag is absent or its id is unknown to the registry.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect; passing an empty stack returns `Optional.empty()` |
| `level` | the level whose registry access is used to resolve the disc |

**Returns:** the disc holder, or `Optional.empty()` when the stack is
not a Backstube disc or the disc is missing from the registry

> **See also:** [`readDisc(ItemStack, HolderLookup.Provider)`](#readdisc-itemstack-holderlookup-provider)
> 
> **Since** 0.1.2

---

### `readDisc(ItemStack, HolderLookup.Provider)`

```java
public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack, HolderLookup.Provider provider)
```

Returns the disc carried by the given stack, if any.

Reads the `BackstubeDisc` NBT tag from `stack` and resolves
it against the `backstube:music_disc` registry visible to
`provider`. Empty when the tag is absent or the disc id is unknown
to the registry.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect; passing an empty stack returns `Optional.empty()` |
| `provider` | a `HolderLookup.Provider`; obtainable from `Level#registryAccess()` or any `net.minecraft.core.RegistryAccess` instance |

**Returns:** the disc holder, or `Optional.empty()` when the stack is
not a Backstube disc or the disc is missing from the registry

> **Since** 0.1.2

---

### `readDescription(ItemStack)`

```java
public static Component readDescription(ItemStack stack)
```

Reads the cached "Artist - Title" component from the stack's NBT.

Returns the value previously written to `BackstubeDescription` by
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder), deserialised from JSON, or
`null` when the tag is absent. Used by Backstube's
`RecordItem` mixin to override `RecordItem.getDisplayName()`
so tooltips and the `Now Playing` message show the disc's
description even when the registry is not available on the client.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect |

**Returns:** the cached description `Component`, or `null` if the
tag is not set

> **Since** 0.1.3

---

### `readRarity(ItemStack)`

```java
public static Rarity readRarity(ItemStack stack)
```

Reads the disc's `Rarity` from the stack's NBT.

Returns the value previously written to `BackstubeRarity` by
[`applyDiscData(ItemStack, Holder)`](#applydiscdata-itemstack-holder), parsed by
`Rarity.valueOf(String)`. Falls back to `Rarity.COMMON` when
the tag is absent or holds an unknown value. Used by Backstube's
`Item` mixin to override `Item.getRarity(ItemStack)` on a
per-stack basis.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect |

**Returns:** the disc's `Rarity`; never `null`

> **Since** 0.1.3

---

### `getAllDiscs(HolderLookup.Provider)`

```java
public static Stream<Holder.Reference<BackstubeMusicDisc>> getAllDiscs(HolderLookup.Provider provider)
```

Streams every disc registered in the `backstube:music_disc` registry
visible to `provider`.

The order matches the registry's natural iteration order. Returns an
empty stream if the registry is not available (for example, before the
world is loaded).

**Parameters:**

| Name | Description |
|---|---|
| `provider` | a `HolderLookup.Provider`; obtainable from `Level#registryAccess()` or any `net.minecraft.core.RegistryAccess` instance |

**Returns:** a non-`null` stream of disc holders

> **Since** 0.1.2

---

### `discSoundLocation(BackstubeMusicDisc, ResourceLocation)`

```java
public static ResourceLocation discSoundLocation(BackstubeMusicDisc disc, ResourceLocation discId)
```

Resolves the audio file id that should play for the given disc.

Equivalent to
`disc.sound().orElse(DiscSound.DEFAULT).resolveName(discId)`: if the
disc supplies a [`DiscSound`](DiscSound.md) with an explicit `name`, that
value is returned verbatim; otherwise the id is derived from
`discId` by prefixing the path with `records/` (see
[`DiscSound.resolveName(ResourceLocation)`](DiscSound.md#resolvename-resourcelocation)).

**Parameters:**

| Name | Description |
|---|---|
| `disc` | the disc data; must not be `null` |
| `discId` | the disc id; must not be `null` |

**Returns:** the resolved sound id; never `null`

> **See also:** [`DiscSound.resolveName(ResourceLocation)`](DiscSound.md#resolvename-resourcelocation)
> 
> **Since** 0.1.2
