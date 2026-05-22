package com.jahirtrap.backstube.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ModJukeboxSongs {
    private static final FileToIdConverter DISC_CONVERTER = FileToIdConverter.json("backstube/music_disc");

    public static Map<Identifier, Resource> injectFromDiscs(ResourceManager manager, Map<Identifier, Resource> original) {
        Map<Identifier, Resource> discs = DISC_CONVERTER.listMatchingResources(manager);
        Map<Identifier, BackstubeMusicDisc> codeDiscs = BackstubeAPI.codeDiscs();
        if (discs.isEmpty() && codeDiscs.isEmpty()) return original;

        Map<Identifier, Resource> merged = new HashMap<>(original);

        for (Map.Entry<Identifier, Resource> entry : discs.entrySet()) {
            Identifier id = DISC_CONVERTER.fileToId(entry.getKey());
            Identifier jukeboxFile = id.withPath("jukebox_song/" + id.getPath() + ".json");
            merged.put(jukeboxFile, wrap(entry.getValue()));
        }

        if (!codeDiscs.isEmpty()) {
            PackResources sampleSource = Stream.concat(discs.values().stream(), original.values().stream())
                    .findFirst().map(Resource::source).orElse(null);
            if (sampleSource != null) {
                for (Map.Entry<Identifier, BackstubeMusicDisc> entry : codeDiscs.entrySet()) {
                    Identifier id = entry.getKey();
                    Identifier jukeboxFile = id.withPath("jukebox_song/" + id.getPath() + ".json");
                    if (merged.containsKey(jukeboxFile)) continue;
                    merged.put(jukeboxFile, synthesizeJukeboxSong(sampleSource, entry.getValue()));
                }
            }
        }

        return merged;
    }

    private static Resource wrap(Resource original) {
        IoSupplier<InputStream> supplier = () -> {
            try (InputStream in = original.open()) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                String artist = extractText(json.get("artist"));
                String title = extractText(json.get("title"));
                json.addProperty("description", artist + " - " + title);
                json.addProperty("sound_event", "backstube:disc");
                if (!json.has("comparator_output")) json.addProperty("comparator_output", 1);
                return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IOException("Failed to transform Backstube disc JSON", e);
            }
        };
        return new Resource(original.source(), supplier) {
            @Override
            public Optional<KnownPack> knownPackInfo() {
                return Optional.empty();
            }
        };
    }

    public static JsonObject buildJukeboxSongJson(BackstubeMusicDisc disc) {
        JsonObject json = new JsonObject();
        json.addProperty("description", disc.artist().getString() + " - " + disc.title().getString());
        json.addProperty("sound_event", "backstube:disc");
        json.addProperty("length_in_seconds", disc.lengthInSeconds());
        json.addProperty("comparator_output", disc.comparatorOutput());
        return json;
    }

    private static Resource synthesizeJukeboxSong(PackResources source, BackstubeMusicDisc disc) {
        JsonObject json = buildJukeboxSongJson(disc);
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        IoSupplier<InputStream> supplier = () -> new ByteArrayInputStream(bytes);
        return new Resource(source, supplier) {
            @Override
            public Optional<KnownPack> knownPackInfo() {
                return Optional.empty();
            }
        };
    }

    private static String extractText(JsonElement element) {
        if (element == null) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonObject()) {
            JsonElement text = element.getAsJsonObject().get("text");
            if (text != null && text.isJsonPrimitive()) return text.getAsString();
        }
        return "";
    }
}
