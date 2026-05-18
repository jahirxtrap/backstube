package com.jahirtrap.backstube.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackstubeModelLoader {

    private static final String MODEL_DIR = "models/item";
    private static final String DISC_PREFIX = "music_disc_";
    private static final String JSON_EXT = ".json";

    private BackstubeModelLoader() {
    }

    public static Set<ResourceLocation> findDiscModels(ResourceManager rm) {
        Map<ResourceLocation, Resource> found = rm.listResources(MODEL_DIR, path -> {
            String p = path.getPath();
            int slash = p.lastIndexOf('/');
            String file = slash >= 0 ? p.substring(slash + 1) : p;
            return file.startsWith(DISC_PREFIX) && file.endsWith(JSON_EXT);
        });
        Set<ResourceLocation> ids = new HashSet<>();
        for (ResourceLocation rl : found.keySet()) {
            String path = rl.getPath();
            String stripped = path.substring(MODEL_DIR.length() - "item".length(), path.length() - JSON_EXT.length());
            ids.add(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), stripped));
        }
        return ids;
    }
}
