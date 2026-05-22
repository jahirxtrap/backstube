# Record `BackstubeMusicDisc`

**Package:** `com.jahirtrap.backstube.api`

```java
public record BackstubeMusicDisc(Component title, Component artist, float lengthInSeconds, int comparatorOutput, Rarity rarity, Optional<Identifier> model, Optional<DiscSound> sound, int stackSize, Optional<Identifier> item)
```

Immutable description of a Backstube music disc.

Each instance is the value of one entry in the
`backstube:music_disc` registry. Discs can be loaded from JSON files
under `data/<namespace>/backstube/music_disc/<path>.json`, registered
from code via [`BackstubeAPI.createDisc(Identifier, BackstubeMusicDisc)`](BackstubeAPI.md#createdisc-identifier-backstubemusicdisc)
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
| `model` | `Optional<Identifier>` |
| `sound` | `Optional<DiscSound>` |
| `stackSize` | `int` |
| `item` | `Optional<Identifier>` |

> **See also**
> 
> - [`BackstubeAPI`](BackstubeAPI.md)
> - [`DiscSound`](DiscSound.md)
> 
> **Since** 0.1.0

---

## Fields

| Field | Description |
|---|---|
| [`REGISTRY_KEY`](#registry_key) | Registry key for the `backstube:music_disc` data registry. |
| [`DIRECT_CODEC`](#direct_codec) | Codec that serialises a full [`BackstubeMusicDisc`](BackstubeMusicDisc.md) as a JSON object. |
| [`DIRECT_STREAM_CODEC`](#direct_stream_codec) | Stream codec that encodes a full [`BackstubeMusicDisc`](BackstubeMusicDisc.md) over the network. |
| [`CODEC`](#codec) | Codec for a `Holder` reference to a registered disc. |
| [`STREAM_CODEC`](#stream_codec) | Stream codec for a `Holder` reference to a registered disc. |

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

> **Since** 0.1.0

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

> **Since** 0.1.0

---

### `DIRECT_STREAM_CODEC`

```java
public static final StreamCodec<RegistryFriendlyByteBuf, BackstubeMusicDisc> DIRECT_STREAM_CODEC
```

Stream codec that encodes a full [`BackstubeMusicDisc`](BackstubeMusicDisc.md) over the
network.

Used by Backstube to sync the data registry from server to client. Useful
for custom payload packets that need to ship a complete disc value rather
than a registry reference.

> **See also:** [`STREAM_CODEC`](#stream_codec)
> 
> **Since** 0.1.0

---

### `CODEC`

```java
public static final Codec<Holder<BackstubeMusicDisc>> CODEC
```

Codec for a `Holder` reference to a registered disc.

Prefer this over [`DIRECT_CODEC`](#direct_codec) when authoring custom data
components, recipes, predicates or any payload that should reference an
existing registry entry by id rather than embedding the whole disc value.

> **Since** 0.1.0

---

### `STREAM_CODEC`

```java
public static final StreamCodec<RegistryFriendlyByteBuf, Holder<BackstubeMusicDisc>> STREAM_CODEC
```

Stream codec for a `Holder` reference to a registered disc.

Counterpart to [`CODEC`](#codec) for network sync; encodes the disc by
registry id, falling back to the full value when the receiving side does
not have the entry.

> **See also:** [`CODEC`](#codec)
> 
> **Since** 0.1.0

---

### `lengthInTicks()`

```java
public int lengthInTicks()
```

Returns the song duration in game ticks.

Computed as `ceil(lengthInSeconds * 20)`; convenient when scheduling
tick-based logic against the song.

**Returns:** the song length expressed in ticks; always `> 0`

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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
| [`model(Identifier)`](#model-identifier) | Sets a custom item-model id used by the rendered disc. |
| [`sound(DiscSound)`](#sound-discsound) | Overrides the audio source and/or playback parameters. |
| [`sound(Identifier)`](#sound-identifier) | Overrides the audio source and/or playback parameters. |
| [`stackSize(int)`](#stacksize-int) | Sets the maximum stack size for the disc item. |
| [`item(Identifier)`](#item-identifier) | Binds the disc data to a specific item id. |
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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

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

> **Since** 0.1.0

---

### `model(Identifier)`

```java
public Builder model(Identifier model)
```

Sets a custom item-model id used by the rendered disc.

The id is resolved through the item-model registry, i.e. it must point at
an entry under `assets/<namespace>/items/<path>.json`. Leaving this
unset uses the generic Backstube model.

**Parameters:**

| Name | Description |
|---|---|
| `model` | the model id |

**Returns:** this builder

> **Since** 0.1.0

---

### `sound(DiscSound)`

```java
public Builder sound(DiscSound sound)
```

Overrides the audio source and/or playback parameters.

The `Identifier` overload is a shortcut that constructs a
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
> **Since** 0.1.0

---

### `sound(Identifier)`

```java
public Builder sound(Identifier soundLocation)
```

Overrides the audio source and/or playback parameters.

The `Identifier` overload is a shortcut that constructs a
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
> **Since** 0.1.0

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

> **Since** 0.1.0

---

### `item(Identifier)`

```java
public Builder item(Identifier item)
```

Binds the disc data to a specific item id.

When set, [`BackstubeAPI.discStack`](BackstubeAPI.md#discstack) and tooltips will use the given
item instead of the generic `backstube:music_disc`. The item must
exist at runtime; if it is missing, Backstube falls back to the generic
item.

Note that [`BackstubeAPI.createDisc(Identifier, BackstubeMusicDisc)`](BackstubeAPI.md#createdisc-identifier-backstubemusicdisc)
already auto-fills this field with the registration id.

**Parameters:**

| Name | Description |
|---|---|
| `item` | the item id to bind to |

**Returns:** this builder

> **Since** 0.1.0

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

> **Since** 0.1.0
