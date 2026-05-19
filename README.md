<h2><strong>Backstube mod</strong></h2>
<p><a href="https://www.curseforge.com/minecraft/mc-mods/backstube"><img src="https://cf.way2muchnoise.eu/full_1543327_downloads.svg?badge_style=flat" alt="CurseForge downloads" /></a> <a href="https://modrinth.com/mod/backstube"><img src="https://img.shields.io/badge/dynamic/json?color=2d2d2d&amp;colorA=17b85a&amp;style=flat-square&amp;label=&amp;suffix= downloads&amp;query=downloads&amp;url=https://api.modrinth.com/v2/project/j0ysmZtJ&amp;logo=modrinth&amp;logoColor=2d2d2d" alt="Modrinth downloads" /></a> <a href="https://central.sonatype.com/artifact/io.github.jahirxtrap/backstube"><img src="https://img.shields.io/maven-central/v/io.github.jahirxtrap/backstube?style=flat" alt="Maven Central" /></a> <a href="https://deepwiki.com/jahirxtrap/backstube"><img src="https://deepwiki.com/badge.svg" alt="Ask DeepWiki" /></a></p>

Bake your own music discs

<strong>Main features:</strong>

<strong>Data-Driven Discs:</strong> Add custom music discs with just a JSON file and an <code>.ogg</code> audio (no Java required)

<strong>Universal Item:</strong> A single <code>music_disc</code> item driven by data components, with audio files auto-resolved at <code>assets/&lt;ns&gt;/sounds/records/&lt;id&gt;.ogg</code>

<strong>Per-Disc Customization:</strong> Title, artist, length, rarity, comparator output, custom item model, and audio config (volume, pitch, attenuation distance)

<strong>Vanilla Compatibility:</strong> Works with all data-pack mechanics (loot tables, trades, commands, predicates)

<strong>Jukebox Loop:</strong> Configurable infinite or fixed-count looping for any song

<strong>Java API:</strong> <code>BackstubeAPI</code> for programmatic disc stack creation, registry access, and dedicated disc items

<strong>Fabric/Quilt dependencies:</strong>

- <a href="https://modrinth.com/mod/modmenu" target="_blank">Mod Menu mod</a> <strong>(Optional)</strong>

### Dependency (Maven Central)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Fabric
    modImplementation 'io.github.jahirxtrap:backstube:TAG-fabric'
    // Forge
    implementation 'io.github.jahirxtrap:backstube:TAG-forge'
    // NeoForge
    implementation 'io.github.jahirxtrap:backstube:TAG-neoforge'
}
```

Replace `TAG` with the version you want (e.g. `26.1.2-0.1.3`).