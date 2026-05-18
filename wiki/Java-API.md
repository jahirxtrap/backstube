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
    Identifier.fromNamespaceAndPath("mymod", "cool_song")
);
Optional<ItemStack> stack = BackstubeAPI.discStack(diskKey, holders);
```

Returns `Optional.empty()` if the disc doesn't exist in the registry (e.g. datapack not loaded).

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
public static final ResourceKey<BackstubeMusicDisc> MY_DISC_KEY =
    ResourceKey.create(BackstubeAPI.discRegistryKey(),
        Identifier.fromNamespaceAndPath(MODID, "my_disc"));

public static final Item MY_DISC_ITEM = registerItem("my_disc", Item::new,
    BackstubeAPI.discProperties(MY_DISC_KEY));
```

The returned `Item.Properties` is pre-configured with:
- `stacksTo(1)`
- `backstube:disc` data component → `MY_DISC_KEY` holder
- `RARITY` data component → the disc's `rarity` field
- `jukeboxPlayable` → the disc's `JukeboxSong`

The data file for `mymod:my_disc` must still exist at `data/mymod/backstube/music_disc/my_disc.json`. The item is just an alternative way to give the disc.

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
    Optional<DiscSound> sound
) { ... }
```

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
| `resolveName(Identifier discId)` | `Identifier` — the effective audio location, falling back to `<ns>:records/<path>` |

`DiscSound.DEFAULT` is provided for the standard playback config.