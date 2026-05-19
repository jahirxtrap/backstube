package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.init.ModJukeboxSongs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class ResourceManagerRegistryLoadTaskMixin {

    @Redirect(method = "lambda$load$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"))
    private Map<Identifier, Resource> injectMusicDiscs(FileToIdConverter converter, ResourceManager manager) {
        Map<Identifier, Resource> original = converter.listMatchingResources(manager);
        ResourceKey<? extends Registry<?>> key = ((ResourceManagerRegistryLoadTask<?>) (Object) this).registryKey();
        if (BackstubeMusicDisc.REGISTRY_KEY.equals(key)) {
            return BackstubeAPI.injectCodeDiscs(converter, original);
        }
        if (!Registries.JUKEBOX_SONG.equals(key)) return original;
        return ModJukeboxSongs.injectFromDiscs(manager, original);
    }
}
