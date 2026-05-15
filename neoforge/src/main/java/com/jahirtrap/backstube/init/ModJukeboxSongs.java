package com.jahirtrap.backstube.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModJukeboxSongs {
    private static final FileToIdConverter DISC_LISTER = FileToIdConverter.json("backstube/music_disc");

    public static Map<Identifier, Resource> injectFromDiscs(ResourceManager rm, Map<Identifier, Resource> original) {
        Map<Identifier, Resource> discs = DISC_LISTER.listMatchingResources(rm);
        if (discs.isEmpty()) return original;
        Map<Identifier, Resource> merged = new HashMap<>(original);
        for (Map.Entry<Identifier, Resource> entry : discs.entrySet()) {
            Identifier id = DISC_LISTER.fileToId(entry.getKey());
            Identifier jukeboxFile = id.withPath("jukebox_song/" + id.getPath() + ".json");
            merged.put(jukeboxFile, wrap(entry.getValue()));
        }
        return merged;
    }

    public static Optional<Resource> findDiscAsJukeboxSong(ResourceKey<?> elementKey, ResourceProvider provider) {
        Identifier id = elementKey.identifier();
        Identifier discResourceId = id.withPath("backstube/music_disc/" + id.getPath() + ".json");
        return provider.getResource(discResourceId).map(ModJukeboxSongs::wrap);
    }

    private static Resource wrap(Resource original) {
        IoSupplier<InputStream> supplier = () -> {
            try (InputStream in = original.open()) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                String artist = extractText(json.get("artist"));
                String title = extractText(json.get("title"));
                json.addProperty("description", artist + " - " + title);
                return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new IOException("Failed to transform Backstube disc JSON", e);
            }
        };
        return new Resource(original.source(), supplier);
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
