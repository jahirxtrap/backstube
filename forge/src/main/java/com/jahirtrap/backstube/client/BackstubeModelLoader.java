package com.jahirtrap.backstube.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class BackstubeModelLoader {

    private static final String DISC_PREFIX = "backstube/music_disc";

    private BackstubeModelLoader() {
    }

    public static Set<ResourceLocation> findDiscModels() {
        Set<ResourceLocation> models = new HashSet<>();
        for (BackstubeMusicDisc disc : BackstubeAPI.codeDiscs().values()) {
            disc.model().map(BackstubeModelLoader::toItemModel).ifPresent(models::add);
        }
        for (Pack pack : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks()) {
            try (PackResources resources = pack.open()) {
                for (String namespace : resources.getNamespaces(PackType.SERVER_DATA)) {
                    resources.listResources(PackType.SERVER_DATA, namespace, DISC_PREFIX, (loc, supplier) -> {
                        if (!loc.getPath().endsWith(".json")) return;
                        try (InputStream in = supplier.get()) {
                            ResourceLocation modelLoc = parseModelField(in);
                            if (modelLoc != null) models.add(toItemModel(modelLoc));
                        } catch (Exception ignored) {
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }
        return models;
    }

    public static ResourceLocation toItemModel(ResourceLocation loc) {
        if (loc.getPath().startsWith("item/")) return loc;
        return new ResourceLocation(loc.getNamespace(), "item/" + loc.getPath());
    }

    private static ResourceLocation parseModelField(InputStream in) {
        JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        if (!json.has("model")) return null;
        return ResourceLocation.tryParse(json.get("model").getAsString());
    }
}
