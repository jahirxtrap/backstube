package com.jahirtrap.backstube.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record DiscSound(
        Optional<ResourceLocation> name,
        float volume,
        float pitch,
        boolean stream,
        int attenuationDistance
) {
    public static final DiscSound DEFAULT = new DiscSound(Optional.empty(), 1.0F, 1.0F, true, 16);

    private static final Codec<DiscSound> OBJECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.optionalFieldOf("name").forGetter(DiscSound::name),
            Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(DiscSound::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(DiscSound::pitch),
            Codec.BOOL.optionalFieldOf("stream", true).forGetter(DiscSound::stream),
            Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(DiscSound::attenuationDistance)
    ).apply(i, DiscSound::new));

    private static final Codec<DiscSound> STRING_CODEC = ResourceLocation.CODEC.xmap(
            id -> new DiscSound(Optional.of(id), 1.0F, 1.0F, true, 16),
            ds -> ds.name.orElseThrow()
    );

    public static final Codec<DiscSound> CODEC = Codec.either(STRING_CODEC, OBJECT_CODEC)
            .xmap(e -> e.map(s -> s, o -> o), Either::right);

    public ResourceLocation resolveName(ResourceLocation discId) {
        return name.orElseGet(() -> new ResourceLocation(discId.getNamespace(), "records/" + discId.getPath()));
    }
}
