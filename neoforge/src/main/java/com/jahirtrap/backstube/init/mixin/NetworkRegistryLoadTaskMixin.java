package com.jahirtrap.backstube.init.mixin;

import com.google.gson.JsonElement;
import com.jahirtrap.backstube.init.ModJukeboxSongs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(NetworkRegistryLoadTask.class)
public abstract class NetworkRegistryLoadTaskMixin {

    @Redirect(method = "lambda$load$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;findAndLoadFromResource(Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/FileToIdConverter;Lnet/minecraft/server/packs/resources/ResourceProvider;)Lcom/mojang/datafixers/util/Either;"))
    private static <T> Either<T, Exception> backstube$injectMusicDisc(Decoder<T> elementDecoder, RegistryOps<JsonElement> ops, ResourceKey<T> elementKey, FileToIdConverter converter, ResourceProvider resourceProvider) {
        if (elementKey.isFor(Registries.JUKEBOX_SONG)) {
            Optional<Resource> disc = ModJukeboxSongs.findDiscAsJukeboxSong(elementKey, resourceProvider);
            if (disc.isPresent()) {
                return RegistryLoadTask.PendingRegistration.loadFromResource(elementDecoder, ops, elementKey, disc.get());
            }
        }
        return RegistryLoadTask.PendingRegistration.findAndLoadFromResource(elementDecoder, ops, elementKey, converter, resourceProvider);
    }
}
