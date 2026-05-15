# Music Discs

## Disc Data File

Each disc is described by a JSON file at:
```
data/<namespace>/backstube/music_disc/<disc_path>.json
```

The disc id is `<namespace>:<disc_path>` (e.g. `mymod:cool_song`).

### Minimal example

```json
{
  "title": "Cool Song",
  "artist": "Author Name",
  "length_in_seconds": 90.0
}
```

### Full example

```json
{
  "title": "Cool Song",
  "artist": "Author Name",
  "length_in_seconds": 90.0,
  "comparator_output": 5,
  "rarity": "epic",
  "model": "mymod:cool_song",
  "sound": {
    "name": "mymod:custom/path",
    "volume": 1.0,
    "pitch": 1.0,
    "stream": true,
    "attenuation_distance": 16
  }
}
```

---

## Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `title` | `Component` | ✓ | — | Disc title (shown in tooltips and "Now Playing" message) |
| `artist` | `Component` | ✓ | — | Artist / author |
| `length_in_seconds` | `float` | ✓ | — | Song duration in seconds. Must match the `.ogg` file or playback will cut off / loop incorrectly |
| `comparator_output` | `int` (0-15) | | `1` | Redstone comparator strength when this disc is in a jukebox |
| `rarity` | `enum` | | `rare` | Item rarity tooltip color: `common`, `uncommon`, `rare`, `epic` |
| `model` | `Identifier` | | auto | Custom items model id (see [Custom Model](#custom-model)) |
| `sound` | `string` or `object` | | auto | Audio source and playback config (see [Sound](#sound)) |

`title` and `artist` accept both plain strings and full `Component` objects:
```json
"title": "Plain Text"
```
```json
"title": { "text": "Styled Text", "color": "gold", "bold": true }
```

---

## Audio Files

### Auto-resolved path

By default, the audio file is resolved as:
```
assets/<namespace>/sounds/records/<disc_path>.ogg
```
where `<namespace>` and `<disc_path>` come from the disc id.

Example: disc id `mymod:cool_song` → audio at `assets/mymod/sounds/records/cool_song.ogg`.

### File format requirements

Minecraft has strict requirements for audio playback:

1. **Codec**: OGG Vorbis (NOT Opus). Files in Opus container play silently.
2. **Channels**: mono (1 channel). Stereo audio is not subject to 3D distance attenuation (it plays at constant volume regardless of how far the player is from the jukebox).

Convert any source audio to Vorbis mono:
```bash
ffmpeg -i input.ogg -ac 1 -c:a libvorbis -q:a 5 output.ogg
```

Verify with `ffprobe`:
```bash
ffprobe -v error -show_entries stream=codec_name,channels -of default=noprint_wrappers=1 file.ogg
# Expected: codec_name=vorbis, channels=1
```

### Loudness normalization

Mojang doesn't publish an official loudness standard for music discs. Measured vanilla discs sit around **-15 to -17 dB mean volume** (roughly **-16 LUFS** integrated). Normalizing your audio to a consistent target keeps the volume even across your collection and avoids jarring jumps when switching discs.

Recommended target:

- **Integrated loudness**: -16 LUFS
- **True peak**: -1.5 dBTP (margin against clipping)

Apply with ffmpeg's `loudnorm` filter (two-pass for accuracy, `linear=true` to only adjust gain without compressing dynamics):

```bash
# Pass 1: measure
ffmpeg -i input.ogg -af "loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json" -f null -

# Pass 2: apply (substitute the measured values from pass 1)
ffmpeg -i input.ogg -af "loudnorm=I=-16:TP=-1.5:LRA=11:measured_I=<I>:measured_TP=<TP>:measured_LRA=<LRA>:measured_thresh=<thresh>:offset=<offset>:linear=true" -ac 1 -c:a libvorbis -q:a 5 output.ogg
```

`linear=true` preserves the original dynamics by applying only a single gain offset. If the file is too out-of-range for pure linear scaling, ffmpeg falls back to dynamic mode (slight compression).

### Creating audio in the browser

[backstube.cc](https://backstube.cc) is a Strudel-based REPL that bakes patterns straight to `.ogg` files compatible with the format requirements. It also handles **mono conversion** and **loudness normalization** automatically, so the output is ready to drop into `assets/<ns>/sounds/records/` without further processing.

---

## Sound

The `sound` field overrides default audio resolution and/or playback parameters. It accepts either a **string** (location override only) or an **object** (full config). All sub-fields are optional.

### String form

```json
"sound": "mymod:custom/song"
```

Equivalent to:
```json
"sound": {
  "name": "mymod:custom/song"
}
```

Useful when you want the audio file at a non-standard location but keep all other defaults.

### Object form

```json
"sound": {
  "name": "mymod:custom/song",
  "volume": 1.0,
  "pitch": 1.0,
  "stream": true,
  "attenuation_distance": 16
}
```

| Sub-field | Type | Default | Description |
|-----------|------|---------|-------------|
| `name` | `Identifier` | `<ns>:records/<path>` | Audio file id. Resolves to `assets/<ns>/sounds/<rest>.ogg` |
| `volume` | `float` | `1.0` | Multiplier on the base jukebox volume (4.0). Higher values increase effective range |
| `pitch` | `float` | `1.0` | Playback pitch |
| `stream` | `boolean` | `true` | Stream the file from disk instead of preloading. Should be `true` for any file longer than a few seconds |
| `attenuation_distance` | `int` | `16` | Base distance for 3D falloff. Effective range = `max(volume, 1.0) × 4.0 × attenuation_distance` |

---

## Custom Model

By default, all discs share the generic Backstube music disc model. Set `model` to override:

```json
"model": "mymod:cool_song"
```

The modder provides:
- `assets/mymod/items/cool_song.json` (item model definition)
- `assets/mymod/models/item/cool_song.json` (model file, can be `parent: item/generated` with custom texture)
- `assets/mymod/textures/item/cool_song.png` (texture file)

This is the same 3-file workflow as any normal Minecraft item.

If `model` is omitted, the generic Backstube model is used as fallback.

---

## Loot Tables, Trades, Commands

Music discs work with all vanilla data-pack mechanisms via the `backstube:disc` data component:

### Loot table entry
```json
{
  "type": "minecraft:item",
  "name": "backstube:music_disc",
  "functions": [{
    "function": "minecraft:set_components",
    "components": { "backstube:disc": "mymod:cool_song" }
  }]
}
```

### Villager trade (datapack)
```json
"sell": {
  "id": "backstube:music_disc",
  "components": { "backstube:disc": "mymod:cool_song" }
}
```

### Predicate / advancement
```json
{
  "items": [{
    "items": "backstube:music_disc",
    "components": { "backstube:disc": "mymod:cool_song" }
  }]
}
```

### Command
```
/give @p backstube:music_disc[backstube:disc="mymod:cool_song"]
```

---

## Registry Tags

`BackstubeMusicDisc` is a registry, so you can group discs with tags at:
```
data/<ns>/tags/backstube/music_disc/<tag>.json
```

Useful for picking a random disc from a group in loot tables, advancements, etc.