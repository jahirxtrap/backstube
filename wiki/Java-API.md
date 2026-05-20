# Java API

For mod developers writing Java code that interacts with Backstube. Pure data-driven discs do **not** need this — see [Music Discs](Music-Discs).

The public API lives in `com.jahirtrap.backstube.api.BackstubeAPI` and `BackstubeMusicDisc`.

---

## Registry Access

### Registry key

```java
ResourceKey<Registry<BackstubeMusicDisc>> key = BackstubeAPI.discRegistryKey();
```

Useful for registering tags, callbacks, datapack registry handlers.

### Data component

```java
DataComponentType<Holder<BackstubeMusicDisc>> component = BackstubeAPI.discComponent();
```

The data component attached to `backstube:music_disc` ItemStacks. Use it to read/write the disc reference on a stack.

### Listing all discs

```java
Stream<Holder.Reference<BackstubeMusicDisc>> discs = BackstubeAPI.getAllDiscs(holders);
```

Requires a `HolderLookup.Provider` (available from `RegistryAccess`, `Level.registryAccess()`, etc.).

---

## Creating ItemStacks

### From a disc holder

```java
Holder<BackstubeMusicDisc> disc = ...;  // from registry lookup
ItemStack stack = BackstubeAPI.discStack(disc, holders);
```

The returned stack has the `backstube:disc` component, `JukeboxPlayable` component, and rarity already set.

### From a registry key

```java
ResourceKey<BackstubeMusicDisc> diskKey = ResourceKey.create(
    BackstubeAPI.discRegistryKey(),
    Identifier.fromNamespaceAndPath("example", "cool_song")
);
Optional<ItemStack> stack = BackstubeAPI.discStack(diskKey, holders);
```

Returns `Optional.empty()` if the disc doesn't exist in the registry (e.g. datapack not loaded).

---

## Registering Discs from Java

For mods that prefer code-based registration over JSON files, `BackstubeAPI.createDisc(...)` registers a disc into Backstube's in-memory registry. Code-registered discs are merged into the data registry at datapack load time. JSON files with the same id take priority over code-registered entries.

### Data only (uses default `backstube:music_disc` item)

```java
@Override
public void onInitialize() {
    BackstubeAPI.createDisc(
        Identifier.fromNamespaceAndPath("example", "cool_song"),
        BackstubeMusicDisc.builder()
            .title("Cool Song")
            .artist("Author")
            .lengthInSeconds(90.0F)
            .build()
    );
}
```

### Data + custom item (one-line, per loader)

The API returns the loader's native item handle so the call can be assigned to a `static final` field:

**Fabric** — returns `Item`:
```java
public static final Item COOL_SONG = BackstubeAPI.createDisc(
    Identifier.fromNamespaceAndPath("example", "cool_song"),
    new Item.Properties().stacksTo(1),
    BackstubeMusicDisc.builder().title("Cool Song").artist("Author").lengthInSeconds(90F).build()
);
```

**Forge** — needs the mod's `DeferredRegister<Item>`, returns `RegistryObject<Item>`:
```java
public static final RegistryObject<Item> COOL_SONG = BackstubeAPI.createDisc(
    MyMod.ITEMS,
    Identifier.fromNamespaceAndPath("example", "cool_song"),
    new Item.Properties().stacksTo(1),
    BackstubeMusicDisc.builder().title("Cool Song").artist("Author").lengthInSeconds(90F).build()
);
```

**NeoForge** — needs the mod's `DeferredRegister.Items`, returns `DeferredItem<Item>`:
```java
public static final DeferredItem<Item> COOL_SONG = BackstubeAPI.createDisc(
    MyMod.ITEMS,
    Identifier.fromNamespaceAndPath("example", "cool_song"),
    new Item.Properties().stacksTo(1),
    BackstubeMusicDisc.builder().title("Cool Song").artist("Author").lengthInSeconds(90F).build()
);
```

### Custom Item subclass

Each loader also has an overload that accepts a `Function<Item.Properties, T>` factory for custom `Item` subclasses:

```java
public static final Item COOL_SONG = BackstubeAPI.createDisc(   // Fabric
    Identifier.fromNamespaceAndPath("example", "cool_song"),
    new Item.Properties().stacksTo(1),
    MyDiscItem::new,                                            // T extends Item
    BackstubeMusicDisc.builder()...build()
);
```

The returned type is parameterized on `T` (fabric: `T`, forge: `RegistryObject<T>`, neoforge: `DeferredItem<T>`).

### Manual two-step (without the helper)

If you prefer to register the item with your loader's own pattern (vanilla `Registry.register`, your own `DeferredRegister`, etc.), do it manually and call `createDisc(id, disc)` separately for the data:

**Fabric:**
```java
public static final ResourceKey<BackstubeMusicDisc> COOL_SONG_KEY =
    ResourceKey.create(BackstubeAPI.discRegistryKey(),
        Identifier.fromNamespaceAndPath("example", "cool_song"));

// 1. Register the item your way
public static final Item COOL_SONG = Registry.register(
    BuiltInRegistries.ITEM,
    Identifier.fromNamespaceAndPath("example", "cool_song"),
    new Item(BackstubeAPI.discProperties(COOL_SONG_KEY))
);

// 2. Register the disc data in mod init
@Override
public void onInitialize() {
    BackstubeAPI.createDisc(
        Identifier.fromNamespaceAndPath("example", "cool_song"),
        BackstubeMusicDisc.builder()...build()
    );
}
```

**Forge:**
```java
public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);

public static final ResourceKey<BackstubeMusicDisc> COOL_SONG_KEY =
    ResourceKey.create(BackstubeAPI.discRegistryKey(),
        Identifier.fromNamespaceAndPath(MODID, "cool_song"));

// 1. Register the item with your own DeferredRegister
public static final RegistryObject<Item> COOL_SONG = ITEMS.register("cool_song",
    () -> new Item(BackstubeAPI.discProperties(COOL_SONG_KEY)));

// 2. Register the disc data in the mod constructor
public MyMod() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    ITEMS.register(bus);
    BackstubeAPI.createDisc(
        Identifier.fromNamespaceAndPath(MODID, "cool_song"),
        BackstubeMusicDisc.builder()...build()
    );
}
```

**NeoForge:**
```java
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

public static final ResourceKey<BackstubeMusicDisc> COOL_SONG_KEY =
    ResourceKey.create(BackstubeAPI.discRegistryKey(),
        Identifier.fromNamespaceAndPath(MODID, "cool_song"));

public static final DeferredItem<Item> COOL_SONG = ITEMS.register("cool_song",
    () -> new Item(BackstubeAPI.discProperties(COOL_SONG_KEY)));

public MyMod(IEventBus bus) {
    ITEMS.register(bus);
    BackstubeAPI.createDisc(
        Identifier.fromNamespaceAndPath(MODID, "cool_song"),
        BackstubeMusicDisc.builder()...build()
    );
}
```

`discProperties(diskKey)` provides the same `delayedComponent` and `jukeboxPlayable` bindings the one-line helper would apply.

### Applying disc data to an arbitrary stack

If you already have an `ItemStack` (any item) and want to mark it as a Backstube disc:

```java
ItemStack stack = new ItemStack(MyMod.SOME_ITEM);
BackstubeAPI.applyDiscData(stack, MY_DISC_KEY, holders);
// stack now has DISC, RARITY, JUKEBOX_PLAYABLE (and MAX_STACK_SIZE if non-default) components set
```

The overload `applyDiscData(ItemStack, Holder<BackstubeMusicDisc>, HolderLookup.Provider)` is also available when you already have the resolved holder.

---

## Reading a disc from a stack

```java
Optional<Holder<BackstubeMusicDisc>> disc = BackstubeAPI.readDisc(itemStack);
disc.ifPresent(d -> {
    Component title = d.value().title();
    Component artist = d.value().artist();
    float length = d.value().lengthInSeconds();
});
```

---

## Registering Your Own Disc Item

If you want a **dedicated item** (not the generic `backstube:music_disc`) that always represents a specific disc — for example, to give it a unique recipe, custom rarity tooltip, or unique creative tab entry — use `discProperties(diskKey)`:

```java
public static final ResourceKey<BackstubeMusicDisc> COOL_SONG_KEY =
    ResourceKey.create(BackstubeAPI.discRegistryKey(),
        Identifier.fromNamespaceAndPath(MODID, "cool_song"));

public static final Item COOL_SONG_ITEM = registerItem("cool_song", Item::new,
    BackstubeAPI.discProperties(COOL_SONG_KEY));
```

The returned `Item.Properties` is pre-configured with:
- `stacksTo(1)`
- `backstube:disc` data component → `COOL_SONG_KEY` holder
- `RARITY` data component → the disc's `rarity` field
- `jukeboxPlayable` → the disc's `JukeboxSong`

The data file for `example:cool_song` must still exist at `data/example/backstube/music_disc/cool_song.json`. The item is just an alternative way to give the disc.

---

## BackstubeMusicDisc Record

The record exposes everything from the data file:

```java
public record BackstubeMusicDisc(
    Component title,
    Component artist,
    float lengthInSeconds,
    int comparatorOutput,
    Rarity rarity,
    Optional<Identifier> model,
    Optional<DiscSound> sound,
    int stackSize,
    Optional<Identifier> item
) { ... }
```

### Builder

For code-based construction, use the fluent builder:

```java
BackstubeMusicDisc disc = BackstubeMusicDisc.builder()
    .title("Cool Song")                                   // String shortcut
    .artist(Component.translatable("song.example.cool"))    // or any Component
    .lengthInSeconds(90.0F)
    .comparatorOutput(5)                                  // optional, default 1
    .rarity(Rarity.EPIC)                                  // optional, default RARE
    .model(Identifier.fromNamespaceAndPath("example", "cool_song"))   // optional
    .sound(Identifier.fromNamespaceAndPath("example", "music/cool"))  // shortcut
    .stackSize(1)                                         // optional, default 1
    .item(Identifier.fromNamespaceAndPath("example", "cool_song"))    // optional
    .build();
```

`title`, `artist`, and `lengthInSeconds` are required; `build()` throws `IllegalStateException` if any is missing.

For `sound`, both `sound(DiscSound)` (full config) and `sound(Identifier)` (location only, defaults applied) are accepted.

Helper methods:

| Method | Returns |
|--------|---------|
| `lengthInTicks()` | `int` — duration in ticks (`ceil(lengthInSeconds × 20)`) |
| `hasFinished(long ticksElapsed)` | `boolean` — true if the song should end (`ticks >= length + 20`) |
| `description()` | `Component` — formatted "Artist - Title" string used in tooltips |

### Codecs

| Field | Use |
|-------|-----|
| `BackstubeMusicDisc.DIRECT_CODEC` | Serialize the full record (used by the registry) |
| `BackstubeMusicDisc.DIRECT_STREAM_CODEC` | Network sync of the full record |
| `BackstubeMusicDisc.CODEC` | `Holder<BackstubeMusicDisc>` codec (use this in your own data components) |
| `BackstubeMusicDisc.STREAM_CODEC` | Network sync of a holder reference |

---

## DiscSound Record

```java
public record DiscSound(
    Optional<Identifier> name,
    float volume,
    float pitch,
    boolean stream,
    int attenuationDistance
) { ... }
```

Helper:

| Method | Returns |
|--------|---------|
| `resolveName(Identifier discId)` | `Identifier` — the effective audio location, falling back to `<namespace>:records/<path>` |

`DiscSound.DEFAULT` is provided for the standard playback config.