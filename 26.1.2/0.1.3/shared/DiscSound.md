# Record `DiscSound`

**Package:** `com.jahirtrap.backstube.api`

```java
public record DiscSound(Optional<Identifier> name, float volume, float pitch, boolean stream, int attenuationDistance)
```

Audio source and playback overrides for a [`BackstubeMusicDisc`](BackstubeMusicDisc.md).

When a disc does not provide a `sound` value, Backstube uses
[`DEFAULT`](#default) (i.e. auto-resolves the audio file from the disc id and
applies the documented playback defaults). When a disc provides one, every
field is optional and falls back to the corresponding default.

**Record components**

- `name` — optional sound id; when absent it is resolved
to `<discNamespace>:records/<discPath>` via
[`resolveName(Identifier)`](#resolvename-identifier).

- `volume` — multiplier applied on top of the base jukebox
volume of `4.0` (default `1.0`). Higher values increase
the effective broadcast range.

- `pitch` — playback pitch (default `1.0`).

- `stream` — stream the file from disk rather than
preloading it (default `true`). Keep this enabled for any
sound longer than a few seconds.

- `attenuationDistance` — base distance for 3D falloff
(default `16`). The effective range is
`max(volume, 1.0) * 4.0 * attenuationDistance`.

**Components:**

| Name | Type |
|---|---|
| `name` | `Optional<Identifier>` |
| `volume` | `float` |
| `pitch` | `float` |
| `stream` | `boolean` |
| `attenuationDistance` | `int` |

> **See also:** [`BackstubeMusicDisc`](BackstubeMusicDisc.md)
> 
> **Since** 0.1.0

---

## Fields

| Field | Description |
|---|---|
| [`DEFAULT`](#default) | The default playback configuration. |
| [`CODEC`](#codec) | Codec accepting either an `Identifier` string (location-only shortcut) or a full JSON object with all fields. |

## Methods

| Method | Summary |
|---|---|
| [`resolveName(Identifier)`](#resolvename-identifier) | Returns the effective audio sound id for a disc, applying the default if [`name`](#name) is empty. |

---

### `DEFAULT`

```java
public static final DiscSound DEFAULT
```

The default playback configuration.

Equivalent to `new DiscSound(Optional.empty(), 1.0F, 1.0F, true, 16)`; used when a disc does not provide a `sound` value of its own.

> **Since** 0.1.0

---

### `CODEC`

```java
public static final Codec<DiscSound> CODEC
```

Codec accepting either an `Identifier` string (location-only
shortcut) or a full JSON object with all fields.

The string form is equivalent to providing only `name`; every other
field is left at its default.

> **Since** 0.1.0

---

### `resolveName(Identifier)`

```java
public Identifier resolveName(Identifier discId)
```

Returns the effective audio sound id for a disc, applying the default if
[`name`](#name) is empty.

When `name` is present, that value is returned verbatim. Otherwise
the id is built from the disc id by keeping its namespace and prefixing
its path with `records/`: a disc id of
`example:cool_song` resolves to `example:records/cool_song`.
Backstube reads the actual OGG from
`assets/<namespace>/sounds/<rest>.ogg`.

**Parameters:**

| Name | Description |
|---|---|
| `discId` | the id of the disc this sound belongs to; must not be `null` |

**Returns:** the resolved sound id; never `null`

> **Since** 0.1.0
