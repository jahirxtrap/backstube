# Backstube

An in-game music framework for Minecraft mods supporting **Fabric**, **Forge**, and **NeoForge**. It provides a single generic `music_disc` item driven by data — modders, datapackers, and resource pack creators can add their own discs without writing a single line of Java.

## Concept

Vanilla Minecraft hardcodes each music disc as a separate item. Backstube unifies them into one item (`backstube:music_disc`) with a `backstube:disc` data component that points to a registry entry describing the song (title, artist, length, optional model, optional sound config). The actual audio file is auto-resolved from the disc id.

A disc can be added in three ways:
- **Data-driven** (no Java) — JSON + `.ogg` only. See [Data-Driven](Data-Driven).
- **Pure Java** — register everything in code via `BackstubeAPI.createDisc(...)`. See [Java API](Java-API#registering-discs-from-java).
- **Hybrid** — Java item + JSON data, or JSON data referencing an externally-registered item via the `item` field. See [Java API](Java-API) and [Data-Driven → Custom Item](Data-Driven#custom-item).

## Setup

### Dependency

The library is published on [Maven Central](https://central.sonatype.com/artifact/io.github.jahirxtrap/backstube).

Add the dependency to your `build.gradle`:

**Fabric:**
```gradle
dependencies {
    modImplementation "io.github.jahirxtrap:backstube:${backstube_version}-fabric"
}
```

**Forge:**
```gradle
dependencies {
    implementation "io.github.jahirxtrap:backstube:${backstube_version}-forge"
}
```

**NeoForge:**
```gradle
dependencies {
    implementation "io.github.jahirxtrap:backstube:${backstube_version}-neoforge"
}
```

In `gradle.properties`:
```properties
backstube_version=26.1.2-0.1.3
```

### Declaring the dependency in mod metadata

**Fabric** (`fabric.mod.json`):
```json
"depends": {
    "backstube": "*"
}
```

**Forge/NeoForge** (`mods.toml` / `neoforge.mods.toml`):
```toml
[[dependencies.${mod_id}]]
    modId = "backstube"
    mandatory = true
    versionRange = "[26.1.2-0.1.0,)"
    ordering = "AFTER"
    side = "BOTH"
```

## Minimal disc example

A complete data-driven disc only needs a JSON file plus an `.ogg`:

`data/example/backstube/music_disc/cool_song.json`:
```json
{
  "title": "Cool Song",
  "artist": "Author Name",
  "length_in_seconds": 90.5
}
```

`assets/example/sounds/records/cool_song.ogg` — Vorbis mono (see [Data-Driven → Audio Files](Data-Driven#audio-files) for format requirements and normalization).

In-game:
```
/give @p backstube:music_disc[backstube:disc="example:cool_song"]
```

For optional fields, audio format, custom models, custom items, and integrations see [Data-Driven](Data-Driven). For code-based registration see [Java API](Java-API).

## Creating audio with Backstube Web

[backstube.cc](https://backstube.cc) is a companion browser tool built on top of [Strudel](https://strudel.cc) that lets you compose patterns and bake them straight to `.ogg` audio files compatible with Backstube — no DAW, no command-line audio tools required.