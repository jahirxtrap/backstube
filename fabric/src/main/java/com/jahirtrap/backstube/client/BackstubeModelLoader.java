package com.jahirtrap.backstube.client;

import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BackstubeModelLoader {

    private static final String MODEL_DIR = "models/item";
    private static final String DISC_PREFIX = "music_disc_";
    private static final String JSON_EXT = ".json";

    private BackstubeModelLoader() {
    }

    public static void init() {
        PreparableModelLoadingPlugin.register(BackstubeModelLoader::loadDiscModels, (ids, ctx) -> ctx.addModels(ids));
    }

    private static CompletableFuture<Set<ResourceLocation>> loadDiscModels(ResourceManager rm, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
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
                ids.add(new ResourceLocation(rl.getNamespace(), stripped));
            }
            return ids;
        }, executor);
    }
}
