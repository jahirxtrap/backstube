package com.jahirtrap.backstube.init.mixin;

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

    @Redirect(method = "loadContentsFromManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"))
    private static Map<Identifier, Resource> injectMusicDiscs(FileToIdConverter lister, ResourceManager rm) {
        Map<Identifier, Resource> original = lister.listMatchingResources(rm);
        if (!JUKEBOX_SONG_PREFIX.equals(lister.prefix)) return original;
        return ModJukeboxSongs.injectFromDiscs(rm, original);
    }
}
