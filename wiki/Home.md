# Backstube

An in-game music framework for Minecraft mods supporting **Fabric**, **Forge**, and **NeoForge**. It provides a single generic `music_disc` item driven by data — modders, datapackers, and resource pack creators can add their own discs without writing a single line of Java.

## Concept

Vanilla Minecraft hardcodes each music disc as a separate item. Backstube unifies them into one item (`backstube:music_disc`) with a `backstube:disc` data component that points to a registry entry describing the song (title, artist, length, optional model, optional sound config). The actual audio file is auto-resolved from the disc id.

This means a disc can be added by:
- A **mod** bundling data + assets (Java optional, only needed for advanced integration)
- A **datapack + resource pack** combo — pure content, zero Java required

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
backstube_version=26.1.2-0.1.0
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

## Quick start

### Adding a music disc — pure data (no Java)

1. **Disc data** at `data/<your_ns>/backstube/music_disc/<disc_id>.json`:
   ```json
   {
     "title": "My Song",
     "artist": "Author Name",
     "length_in_seconds": 90.5
   }
   ```

2. **Audio file** at `assets/<your_ns>/sounds/records/<disc_id>.ogg` (Vorbis mono — see [Music Discs](Music-Discs#audio-files))

3. Distribute as a mod (bundled), datapack + resource pack combo, or even via `/datapack enable` on a server.

The disc is now usable in-game:
```
/give @p backstube:music_disc[backstube:disc="<your_ns>:<disc_id>"]
```

See [Music Discs](Music-Discs) for the full disc data format (optional fields, sound config, custom model).

### Java integration (optional)

If your mod needs to programmatically create disc stacks, query the registry, or register its own music disc item, use the `BackstubeAPI`. See [Java API](Java-API).

## Creating audio with Backstube Web

[backstube.cc](https://backstube.cc) is a companion browser tool built on top of [Strudel](https://strudel.cc) that lets you compose patterns and bake them straight to `.ogg` audio files compatible with Backstube — no DAW, no command-line audio tools required.