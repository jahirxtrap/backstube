# Record `BackstubeMusicDisc`

**Package:** `com.jahirtrap.backstube.api`

```java
public record BackstubeMusicDisc(Component title, Component artist, float lengthInSeconds, int comparatorOutput, Rarity rarity, Optional<ResourceLocation> model, Optional<DiscSound> sound, int stackSize, Optional<ResourceLocation> item)
```

Immutable description of a Backstube music disc.

Each instance is the value of one entry in the
`backstube:music_disc` registry. Discs can be loaded from JSON files
under `data/<namespace>/backstube/music_disc/<path>.json`, registered
from code via [`BackstubeAPI.createDisc(ResourceLocation, BackstubeMusicDisc)`](BackstubeAPI.md#createdisc-resourcelocation-backstubemusicdisc)
or both.

Use [`builder()`](#builder) to construct an instance fluently; only
`title`, `artist` and `lengthInSeconds` are mandatory.

**Record components**

- `title` — the disc title; shown in tooltips and in the
`Now Playing` message.

- `artist` — the song author.

- `lengthInSeconds` — song duration; must be `> 0`
and should match the OGG file's actual length.

- `comparatorOutput` — redstone comparator strength when
the disc is in a jukebox; must lie in `[0, 15]` (default
`1`).

- `rarity` — the `Rarity` of the item tooltip
(default `Rarity.RARE`).

- `model` — optional custom item model id; when absent the
generic Backstube model is used.

- `sound` — optional [`DiscSound`](DiscSound.md) overriding the
audio source and playback parameters.

- `stackSize` — max stack size, in `[1, 64]`
(default `1`).

- `item` — optional custom item id; when absent the
generic `backstube:music_disc` item is used.

**Components:**

| Name | Type |
|---|---|
| `title` | `Component` |
| `artist` | `Component` |
| `lengthInSeconds` | `float` |
| `comparatorOutput` | `int` |
| `rarity` | `Rarity` |
| `model` | `Optional<ResourceLocation>` |
| `sound` | `Optional<DiscSound>` |
| `stackSize` | `int` |
| `item` | `Optional<ResourceLocation>` |

> **See also**
> 
> - [`BackstubeAPI`](BackstubeAPI.md)
> - [`DiscSound`](DiscSound.md)
> 
> **Since** 0.1.2

---

## Fields

| Field | Description |
|---|---|
| [`REGISTRY_KEY`](#registry_key) | Registry key for the `backstube:music_disc` data registry. |
| [`RARITY_CODEC`](#rarity_codec) | Lowercase-string codec for `Rarity`. |
| [`DIRECT_CODEC`](#direct_codec) | Codec that serialises a full [`BackstubeMusicDisc`](BackstubeMusicDisc.md) as a JSON object. |
| [`CODEC`](#codec) | Codec for a `Holder` reference to a registered disc. |

## Methods

| Method | Summary |
|---|---|
| [`lengthInTicks()`](#lengthinticks) | Returns the song duration in game ticks. |
| [`hasFinished(long)`](#hasfinished-long) | Returns whether the song has played to completion, given how many ticks have elapsed since playback started. |
| [`description()`](#description) | Returns the formatted "Artist - Title" component used as the disc's tooltip subtitle and `Now Playing` message. |
| [`builder()`](#builder) | Returns a new `Builder` for fluently constructing a [`BackstubeMusicDisc`](BackstubeMusicDisc.md). |

---

### `REGISTRY_KEY`

```java
public static final ResourceKey<Registry<BackstubeMusicDisc>> REGISTRY_KEY
```

Registry key for the `backstube:music_disc` data registry.

Mirrors [`BackstubeAPI.discRegistryKey()`](BackstubeAPI.md#discregistrykey) for convenience — use
whichever reads better at the call site.

> **Since** 0.1.2

---

### `RARITY_CODEC`

```java
public static final Codec<Rarity> RARITY_CODEC
```

Lowercase-string codec for `Rarity`.

Required because MC 1.20.1 does not yet provide a public `Rarity.CODEC`;
Backstube uses this codec for the `rarity` field of
[`DIRECT_CODEC`](#direct_codec). Input names are matched case-insensitively against
`Rarity.valueOf(String)`; output names are emitted lowercase.

> **Since** 0.1.2

---

### `DIRECT_CODEC`

```java
public static final Codec<BackstubeMusicDisc> DIRECT_CODEC
```

Codec that serialises a full [`BackstubeMusicDisc`](BackstubeMusicDisc.md) as a JSON object.

The format matches the on-disk schema used by data-driven discs (see the
package documentation for field reference). Used by the registry to load
JSON files; also handy when writing custom serialisers (for example, a
datapack-generated set of discs).

> **Since** 0.1.2

---

### `CODEC`

```java
public static final Codec<Holder<BackstubeMusicDisc>> CODEC
```

Codec for a `Holder` reference to a registered disc.

Prefer this over [`DIRECT_CODEC`](#direct_codec) when authoring recipes, predicates
or any payload that should reference an existing registry entry by id
rather than embedding the whole disc value.

> **Since** 0.1.2

---

### `lengthInTicks()`

```java
public int lengthInTicks()
```

Returns the song duration in game ticks.

Computed as `ceil(lengthInSeconds * 20)`; convenient when scheduling
tick-based logic against the song.

**Returns:** the song length expressed in ticks; always `> 0`

> **Since** 0.1.2

---

### `hasFinished(long)`

```java
public boolean hasFinished(long ticksElapsed)
```

Returns whether the song has played to completion, given how many ticks
have elapsed since playback started.

Uses a 20-tick grace period (one second) past [`lengthInTicks()`](#lengthinticks) so
that short rounding errors at the end of the OGG do not trigger an early
stop.

**Parameters:**

| Name | Description |
|---|---|
| `ticksElapsed` | ticks since the jukebox started playing this disc |

**Returns:** `true` when `ticksElapsed >= lengthInTicks() + 20`

> **Since** 0.1.2

---

### `description()`

```java
public Component description()
```

Returns the formatted "Artist - Title" component used as the disc's
tooltip subtitle and `Now Playing` message.

The result is a fresh component constructed by concatenating
[`artist()`](#artist), the literal `" - "` separator and [`title()`](#title).

**Returns:** a non-`null` component; never empty

> **Since** 0.1.2

---

### `builder()`

```java
public static Builder builder()
```

Returns a new `Builder` for fluently constructing a
[`BackstubeMusicDisc`](BackstubeMusicDisc.md).

The builder pre-fills sensible defaults for every optional component; only
`Builder.title`, `Builder.artist` and
`Builder.lengthInSeconds` are required before calling
`Builder.build()`.

**Returns:** a fresh builder instance; never `null`

> **Since** 0.1.3

---

## Nested Class `Builder`

```java
public static final class Builder
```

Fluent builder for [`BackstubeMusicDisc`](BackstubeMusicDisc.md).

Mandatory fields are `title`, `artist` and
`lengthInSeconds`; all others fall back to documented defaults if
left unset. Reuse a single builder for multiple discs by overriding fields
between `build()` calls; the builder is mutable but not
thread-safe.

> **Since** 0.1.3

---

## Methods

| Method | Summary |
|---|---|
| [`title(Component)`](#title-component) | Sets the disc title. |
| [`title(String)`](#title-string) | Sets the disc title. |
| [`artist(Component)`](#artist-component) | Sets the song artist. |
| [`artist(String)`](#artist-string) | Sets the song artist. |
| [`lengthInSeconds(float)`](#lengthinseconds-float) | Sets the song duration in seconds. |
| [`comparatorOutput(int)`](#comparatoroutput-int) | Sets the redstone comparator output strength when the disc is in a jukebox. |
| [`rarity(Rarity)`](#rarity-rarity) | Sets the `Rarity` used for the disc's item tooltip colour. |
| [`model(ResourceLocation)`](#model-resourcelocation) | Sets a custom item-model id used by the rendered disc. |
| [`sound(DiscSound)`](#sound-discsound) | Overrides the audio source and/or playback parameters. |
| [`sound(ResourceLocation)`](#sound-resourcelocation) | Overrides the audio source and/or playback parameters. |
| [`stackSize(int)`](#stacksize-int) | Sets the maximum stack size for the disc item. |
| [`item(ResourceLocation)`](#item-resourcelocation) | Binds the disc data to a specific item id. |
| [`build()`](#build) | Builds the immutable [`BackstubeMusicDisc`](BackstubeMusicDisc.md) instance. |

---

### `title(Component)`

```java
public Builder title(Component title)
```

Sets the disc title.

The `String` overload wraps the value in
`Component.literal(String)`.

**Parameters:**

| Name | Description |
|---|---|
| `title` | the title text |

**Returns:** this builder

> **Since** 0.1.3

---

### `title(String)`

```java
public Builder title(String literal)
```

Sets the disc title.

The `String` overload wraps the value in
`Component.literal(String)`.

**Parameters:**

| Name | Description |
|---|---|
| `literal` | the title text |

**Returns:** this builder

> **Since** 0.1.3

---

### `artist(Component)`

```java
public Builder artist(Component artist)
```

Sets the song artist.

The `String` overload wraps the value in
`Component.literal(String)`.

**Parameters:**

| Name | Description |
|---|---|
| `artist` | the artist text |

**Returns:** this builder

> **Since** 0.1.3

---

### `artist(String)`

```java
public Builder artist(String literal)
```

Sets the song artist.

The `String` overload wraps the value in
`Component.literal(String)`.

**Parameters:**

| Name | Description |
|---|---|
| `literal` | the artist text |

**Returns:** this builder

> **Since** 0.1.3

---

### `lengthInSeconds(float)`

```java
public Builder lengthInSeconds(float seconds)
```

Sets the song duration in seconds.

Must match the OGG file's actual length to within a tick or playback may
be cut short or looped incorrectly. The value passed to [`build()`](#build)
must be strictly greater than zero.

**Parameters:**

| Name | Description |
|---|---|
| `seconds` | the duration |

**Returns:** this builder

> **Since** 0.1.3

---

### `comparatorOutput(int)`

```java
public Builder comparatorOutput(int comparatorOutput)
```

Sets the redstone comparator output strength when the disc is in a
jukebox.

Must be in the range `[0, 15]` (validated by the codec when the
value is serialised). Defaults to `1`.

**Parameters:**

| Name | Description |
|---|---|
| `comparatorOutput` | the comparator strength |

**Returns:** this builder

> **Since** 0.1.3

---

### `rarity(Rarity)`

```java
public Builder rarity(Rarity rarity)
```

Sets the `Rarity` used for the disc's item tooltip colour.

Defaults to `Rarity.RARE`.

**Parameters:**

| Name | Description |
|---|---|
| `rarity` | the rarity |

**Returns:** this builder

> **Since** 0.1.3

---

### `model(ResourceLocation)`

```java
public Builder model(ResourceLocation model)
```

Sets a custom item-model id used by the rendered disc.

The id is resolved through the item-model registry, i.e. it must point at
an entry under `assets/<namespace>/models/item/<path>.json`. Leaving
this unset uses the generic Backstube model.

**Parameters:**

| Name | Description |
|---|---|
| `model` | the model id |

**Returns:** this builder

> **Since** 0.1.3

---

### `sound(DiscSound)`

```java
public Builder sound(DiscSound sound)
```

Overrides the audio source and/or playback parameters.

The `ResourceLocation` overload is a shortcut that constructs a
[`DiscSound`](DiscSound.md) with the supplied name and all other parameters set to
their defaults (`volume=1.0`, `pitch=1.0`, `stream=true`,
`attenuationDistance=16`).

**Parameters:**

| Name | Description |
|---|---|
| `sound` | the sound configuration |

**Returns:** this builder

> **See also:** [`DiscSound`](DiscSound.md)
> 
> **Since** 0.1.3

---

### `sound(ResourceLocation)`

```java
public Builder sound(ResourceLocation soundLocation)
```

Overrides the audio source and/or playback parameters.

The `ResourceLocation` overload is a shortcut that constructs a
[`DiscSound`](DiscSound.md) with the supplied name and all other parameters set to
their defaults (`volume=1.0`, `pitch=1.0`, `stream=true`,
`attenuationDistance=16`).

**Parameters:**

| Name | Description |
|---|---|
| `soundLocation` | the sound configuration |

**Returns:** this builder

> **See also:** [`DiscSound`](DiscSound.md)
> 
> **Since** 0.1.3

---

### `stackSize(int)`

```java
public Builder stackSize(int stackSize)
```

Sets the maximum stack size for the disc item.

Must be in the range `[1, 64]` (validated by the codec when the
value is serialised). Defaults to `1`, matching vanilla music discs.

**Parameters:**

| Name | Description |
|---|---|
| `stackSize` | the max stack size |

**Returns:** this builder

> **Since** 0.1.3

---

### `item(ResourceLocation)`

```java
public Builder item(ResourceLocation item)
```

Binds the disc data to a specific item id.

When set, [`BackstubeAPI.discStack`](BackstubeAPI.md#discstack) and tooltips will use the given
item instead of the generic `backstube:music_disc`. The item must
exist at runtime; if it is missing, Backstube falls back to the generic
item.

Note that [`BackstubeAPI.createDisc(ResourceLocation, BackstubeMusicDisc)`](BackstubeAPI.md#createdisc-resourcelocation-backstubemusicdisc)
already auto-fills this field with the registration id.

**Parameters:**

| Name | Description |
|---|---|
| `item` | the item id to bind to |

**Returns:** this builder

> **Since** 0.1.3

---

### `build()`

```java
public BackstubeMusicDisc build()
```

Builds the immutable [`BackstubeMusicDisc`](BackstubeMusicDisc.md) instance.

**Returns:** the constructed disc

**Throws:**

| Type | Condition |
|---|---|
| `NullPointerException` | if `title` or `artist` was not set |
| `IllegalStateException` | if `lengthInSeconds` was not set or is not strictly positive |

> **Since** 0.1.3
