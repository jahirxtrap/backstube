package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
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
    private static final String DISC_PREFIX = BackstubeMusicDisc.REGISTRY_KEY.location().getNamespace()
            + "/" + BackstubeMusicDisc.REGISTRY_KEY.location().getPath();

    @Redirect(method = "loadRegistryContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"))
    private static Map<ResourceLocation, Resource> injectMusicDiscs(FileToIdConverter converter, ResourceManager manager) {
        Map<ResourceLocation, Resource> original = converter.listMatchingResources(manager);
        if (DISC_PREFIX.equals(converter.prefix)) return BackstubeAPI.injectCodeDiscs(converter, original);
        return original;
    }
}
