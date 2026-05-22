# Class `BackstubeAPI`

**Package:** `com.jahirtrap.backstube.api`

```java
public class BackstubeAPI
```

Public entry point for the Backstube music disc framework.

Backstube unifies every music disc into a single `backstube:music_disc`
item driven by a [`BackstubeMusicDisc`](BackstubeMusicDisc.md) data component. Mods, datapacks
and resource packs add new discs by registering a [`BackstubeMusicDisc`](BackstubeMusicDisc.md)
under a unique id; the disc's audio file is auto-resolved from that id.

Discs can be added in three ways:

- **Data-driven** — ship a JSON file under
`data/<namespace>/backstube/music_disc/<path>.json` plus an OGG
Vorbis mono audio asset; no Java code required.

- **Pure Java** — register everything in code via
[`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc) or one of its
item-aware overloads.

- **Hybrid** — register a custom item with
[`discProperties(ResourceKey)`](#discproperties-resourcekey) and provide the disc data in
JSON, optionally pointing back to the item via the `item`
field.

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
| [`discComponent()`](#disccomponent) | Returns the `backstube:disc` data component type attached to `backstube:music_disc` item stacks. |
| [`discProperties(ResourceKey<BackstubeMusicDisc>)`](#discproperties-resourcekey-backstubemusicdisc) | Builds `Item.Properties` pre-configured for an item that should be recognised as a jukebox-playable disc bound to `diskKey`. |
| [`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc) | Registers a code-based disc backed by the generic `backstube:music_disc` item. |
| [`createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-backstubemusicdisc) | Registers a code-based disc together with a custom `Item` bound to its registry entry. |
| [`createDisc(DeferredRegister.Items, Identifier, Item.Properties, Function<Item.Properties, T>, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-function-item-properties-t-backstubemusicdisc) | Registers a code-based disc together with a custom `Item` subclass. |
| [`codeDiscs()`](#codediscs) | Returns an unmodifiable-style view of every disc registered via [`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc) since startup. |
| [`injectCodeDiscs(FileToIdConverter, Map<Identifier, Resource>)`](#injectcodediscs-filetoidconverter-map-identifier-resource) | Merges code-registered discs into a resource-listing map produced by a `FileToIdConverter`, returning a new map. |
| [`discStack(Holder<BackstubeMusicDisc>, HolderLookup.Provider)`](#discstack-holder-backstubemusicdisc-holderlookup-provider) | Builds a fresh `ItemStack` representing the given disc, ready to be inserted into an inventory. |
| [`discStack(ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`](#discstack-resourcekey-backstubemusicdisc-holderlookup-provider) | Convenience overload that resolves `diskKey` through `provider` and builds the corresponding stack. |
| [`applyDiscData(ItemStack, Holder<BackstubeMusicDisc>, HolderLookup.Provider)`](#applydiscdata-itemstack-holder-backstubemusicdisc-holderlookup-provider) | Stamps the disc-related data components on an existing stack in place. |
| [`applyDiscData(ItemStack, ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`](#applydiscdata-itemstack-resourcekey-backstubemusicdisc-holderlookup-provider) | Resolves `diskKey` through `provider` and stamps the data on `stack`, returning whether the disc was found. |
| [`isDisc(ItemStack)`](#isdisc-itemstack) | Returns whether the given stack carries Backstube disc data. |
| [`readDisc(ItemStack)`](#readdisc-itemstack) | Returns the disc carried by the given stack, if any. |
| [`getAllDiscs(HolderLookup.Provider)`](#getalldiscs-holderlookup-provider) | Streams every disc registered in the `backstube:music_disc` registry visible to `provider`. |

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

### `discComponent()`

```java
public static DataComponentType<Holder<BackstubeMusicDisc>> discComponent()
```

Returns the `backstube:disc` data component type attached to
`backstube:music_disc` item stacks.

The component value is a `Holder` that resolves to the registered
[`BackstubeMusicDisc`](BackstubeMusicDisc.md). Read it with
`stack.get(BackstubeAPI.discComponent())` or use
[`readDisc(ItemStack)`](#readdisc-itemstack) for a null-safe wrapper.

**Returns:** the data component type; never `null`

> **See also:** [`readDisc(ItemStack)`](#readdisc-itemstack)
> 
> **Since** 0.1.2

---

### `discProperties(ResourceKey<BackstubeMusicDisc>)`

```java
public static Item.Properties discProperties(ResourceKey<BackstubeMusicDisc> diskKey)
```

Builds `Item.Properties` pre-configured for an item that should be
recognised as a jukebox-playable disc bound to `diskKey`.

The returned properties have:

- `stacksTo(1)`;

- `jukeboxPlayable` set to the matching jukebox song.

The `backstube:disc` and `minecraft:rarity` components are not
applied to the properties returned here; they are stamped on the stack at
creation time by
[`applyDiscData(ItemStack, Holder, HolderLookup.Provider)`](#applydiscdata-itemstack-holder-holderlookup-provider) (the
helper invoked by [`discStack(Holder, HolderLookup.Provider)`](#discstack-holder-holderlookup-provider)). When
registering a custom item subclass via this method, the disc data must
still exist either as a JSON file or as a code-registered entry via
[`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc).

**Parameters:**

| Name | Description |
|---|---|
| `diskKey` | the disc registry key the item should be bound to; must not be `null` |

**Returns:** a fresh `Item.Properties` ready to be passed to an item
constructor; never `null`

> **See also:** [`createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-backstubemusicdisc)
> 
> **Since** 0.1.2

---

### `createDisc(Identifier, BackstubeMusicDisc)`

```java
public static void createDisc(Identifier id, BackstubeMusicDisc disc)
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
| `id` | the disc id, e.g. `Identifier.fromNamespaceAndPath("example", "cool_song")`; must not be `null` |
| `disc` | the disc data, typically built with [`BackstubeMusicDisc.builder()`](BackstubeMusicDisc.md#builder); must not be `null` |

> **See also:** [`createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-backstubemusicdisc)
> 
> **Since** 0.1.3

---

### `createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)`

```java
public static DeferredItem<Item> createDisc(DeferredRegister.Items register, Identifier id, Item.Properties properties, BackstubeMusicDisc disc)
```

Registers a code-based disc together with a custom `Item` bound to
its registry entry.

Equivalent to calling [`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc)
and then registering an `Item` under `id` with the supplied
`properties` enriched with the `backstube:disc` component,
`minecraft:rarity` component and `jukeboxPlayable` binding.

The item is deferred through the caller-provided
`DeferredRegister.Items` and constructed lazily when the NeoForge
registry event fires; assign the returned `DeferredItem` to a
`static final` field so static initialization order is preserved.

**Parameters:**

| Name | Description |
|---|---|
| `register` | the mod's item `DeferredRegister.Items`; must not be `null` and must already be attached to the mod event bus |
| `id` | the disc id, also used as the item id |
| `properties` | base item properties; `stacksTo` and the disc components are applied automatically |
| `disc` | the disc data |

**Returns:** a `DeferredItem<Item>` pointing at the deferred item;
never `null`

> **See also:** [`createDisc(DeferredRegister.Items, Identifier, Item.Properties, Function, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-function-backstubemusicdisc)
> 
> **Since** 0.1.3

---

### `createDisc(DeferredRegister.Items, Identifier, Item.Properties, Function<Item.Properties, T>, BackstubeMusicDisc)`

```java
public static <T extends Item> DeferredItem<T> createDisc(DeferredRegister.Items register, Identifier id, Item.Properties properties, Function<Item.Properties, T> factory, BackstubeMusicDisc disc)
```

Registers a code-based disc together with a custom `Item` subclass.

Identical to
[`createDisc(DeferredRegister.Items, Identifier, Item.Properties, BackstubeMusicDisc)`](#createdisc-deferredregister-items-identifier-item-properties-backstubemusicdisc) but
the item is constructed by the caller-supplied `factory`, allowing
arbitrary `Item` subclasses (e.g. an item with custom tooltip lines
or use behaviour).

**Parameters:**

| Name | Description |
|---|---|
| `register` | the mod's item `DeferredRegister.Items`; must not be `null` and must already be attached to the mod event bus |
| `id` | the disc id, also used as the item id |
| `properties` | base item properties; disc-related components are added before `factory` is invoked |
| `factory` | constructor reference for the `Item` subclass, e.g. `MyDiscItem::new` |
| `disc` | the disc data |

**Returns:** a `DeferredItem<T>` pointing at the deferred item,
statically typed to `T`

> **Since** 0.1.3

---

### `codeDiscs()`

```java
public static Map<Identifier, BackstubeMusicDisc> codeDiscs()
```

Returns an unmodifiable-style view of every disc registered via
[`createDisc(Identifier, BackstubeMusicDisc)`](#createdisc-identifier-backstubemusicdisc) since startup.

Intended primarily for Backstube's own internal pipelines (resource pack
injection, jukebox song synthesis). External consumers should not rely on
the map for state checks — query the live registry through a
`HolderLookup.Provider` instead.

**Returns:** the map of code-registered discs; insertion-ordered

> **Since** 0.1.3

---

### `injectCodeDiscs(FileToIdConverter, Map<Identifier, Resource>)`

```java
public static Map<Identifier, Resource> injectCodeDiscs(FileToIdConverter converter, Map<Identifier, Resource> original)
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

### `discStack(Holder<BackstubeMusicDisc>, HolderLookup.Provider)`

```java
public static ItemStack discStack(Holder<BackstubeMusicDisc> disc, HolderLookup.Provider provider)
```

Builds a fresh `ItemStack` representing the given disc, ready to be
inserted into an inventory.

The `Item` of the stack is chosen as follows:

1. If the disc's [`BackstubeMusicDisc.item()`](BackstubeMusicDisc.md#item) is present and
resolves in `BuiltInRegistries.ITEM`, that item is used.

2. Otherwise the generic `backstube:music_disc` item is used.

The returned stack already has the `backstube:disc`,
`minecraft:rarity`, optionally `minecraft:max_stack_size` and
`minecraft:jukebox_playable` components set via
[`applyDiscData(ItemStack, Holder, HolderLookup.Provider)`](#applydiscdata-itemstack-holder-holderlookup-provider).

**Parameters:**

| Name | Description |
|---|---|
| `disc` | the resolved disc holder; must carry a registry key (i.e. must originate from a registry lookup, not `Holder.direct(Object)`) |
| `provider` | a `HolderLookup.Provider`; obtainable from `Level#registryAccess()` or `RegistryAccess` |

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

> **See also:** [`discStack(Holder, HolderLookup.Provider)`](#discstack-holder-holderlookup-provider)
> 
> **Since** 0.1.2

---

### `applyDiscData(ItemStack, Holder<BackstubeMusicDisc>, HolderLookup.Provider)`

```java
public static void applyDiscData(ItemStack stack, Holder<BackstubeMusicDisc> disc, HolderLookup.Provider provider)
```

Stamps the disc-related data components on an existing stack in place.

Sets:

- `backstube:disc` → `disc`;

- `minecraft:rarity` → [`BackstubeMusicDisc.rarity()`](BackstubeMusicDisc.md#rarity);

- `minecraft:max_stack_size` →
[`BackstubeMusicDisc.stackSize()`](BackstubeMusicDisc.md#stacksize) (only when the disc's stack
size differs from the vanilla item default of 64);

- `minecraft:jukebox_playable` → the
`net.minecraft.world.item.JukeboxPlayable` matching the disc
id, when one is available in the registry seen by
`provider`.

Useful when starting from an arbitrary stack (for example, one created by
a recipe or a creative-tab generator) and turning it into a Backstube
disc. For a fresh stack prefer
[`discStack(Holder, HolderLookup.Provider)`](#discstack-holder-holderlookup-provider).

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to mutate; must not be `null` |
| `disc` | the disc holder; must carry a registry key |
| `provider` | a `HolderLookup.Provider` used to look up the matching jukebox song |

**Throws:**

| Type | Condition |
|---|---|
| `java.util.NoSuchElementException` | if `disc` has no registry key |

> **Since** 0.1.3

---

### `applyDiscData(ItemStack, ResourceKey<BackstubeMusicDisc>, HolderLookup.Provider)`

```java
public static boolean applyDiscData(ItemStack stack, ResourceKey<BackstubeMusicDisc> diskKey, HolderLookup.Provider provider)
```

Resolves `diskKey` through `provider` and stamps the data on
`stack`, returning whether the disc was found.

Equivalent to looking the key up first and then calling
[`applyDiscData(ItemStack, Holder, HolderLookup.Provider)`](#applydiscdata-itemstack-holder-holderlookup-provider), but
fails silently and reports the result via the return value when the disc
is unknown.

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

Equivalent to checking for the presence of the
[backstube:disc](#disccomponent) component on the stack.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to test; passing an empty stack returns `false` |

**Returns:** `true` when the stack has the `backstube:disc`
component

> **Since** 0.1.3

---

### `readDisc(ItemStack)`

```java
public static Optional<Holder<BackstubeMusicDisc>> readDisc(ItemStack stack)
```

Returns the disc carried by the given stack, if any.

Wraps the stack's `backstube:disc` component lookup in an
`Optional`; empty when the component is absent.

**Parameters:**

| Name | Description |
|---|---|
| `stack` | the stack to inspect; passing an empty stack returns `Optional.empty()` |

**Returns:** the disc holder, or `Optional.empty()` when the stack is
not a Backstube disc

> **Since** 0.1.2

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
