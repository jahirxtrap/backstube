package com.jahirtrap.backstube.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackstubeModelLoader {

    private static final FileToIdConverter DISC_CONVERTER = FileToIdConverter.json("backstube/music_disc");

    private BackstubeModelLoader() {
    }

    public static Set<ResourceLocation> findDiscModels(ResourceManager manager) {
        Set<ResourceLocation> models = new HashSet<>();
        for (Map.Entry<ResourceLocation, Resource> entry : DISC_CONVERTER.listMatchingResources(manager).entrySet()) {
            ResourceLocation modelLoc = extractModel(entry.getValue());
            if (modelLoc != null) models.add(modelLoc);
        }
        for (BackstubeMusicDisc disc : BackstubeAPI.codeDiscs().values()) {
            disc.model().ifPresent(models::add);
        }
        return models;
    }

    private static ResourceLocation extractModel(Resource resource) {
        try (InputStream in = resource.open()) {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!json.has("model")) return null;
            return ResourceLocation.tryParse(json.get("model").getAsString());
        } catch (Exception e) {
            return null;
        }
    }
}
