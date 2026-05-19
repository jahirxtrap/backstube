package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.init.ModJukeboxSongs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {

    @Unique
    private static final String JUKEBOX_SONG_PREFIX = Registries.elementsDirPath(Registries.JUKEBOX_SONG);

    @Unique
    private static final String DISC_PREFIX = BackstubeMusicDisc.REGISTRY_KEY.identifier().getNamespace()
            + "/" + BackstubeMusicDisc.REGISTRY_KEY.identifier().getPath();

    @Redirect(method = "loadContentsFromManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"))
    private static Map<Identifier, Resource> injectMusicDiscs(FileToIdConverter converter, ResourceManager manager) {
        Map<Identifier, Resource> original = converter.listMatchingResources(manager);
        if (DISC_PREFIX.equals(converter.prefix)) return BackstubeAPI.injectCodeDiscs(converter, original);
        if (!JUKEBOX_SONG_PREFIX.equals(converter.prefix)) return original;
        return ModJukeboxSongs.injectFromDiscs(manager, original);
    }
}
