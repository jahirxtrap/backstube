package com.jahirtrap.backstube.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Audio source and playback overrides for a {@link BackstubeMusicDisc}.
 * <p>
 * When a disc does not provide a {@code sound} value, Backstube uses
 * {@link #DEFAULT} (i.e. auto-resolves the audio file from the disc id and
 * applies the documented playback defaults). When a disc provides one, every
 * field is optional and falls back to the corresponding default.
 * <p>
 *
 * @param name                optional sound id; when absent it is resolved
 *                            to {@code <discNamespace>:records/<discPath>}
 *                            via {@link #resolveName(ResourceLocation)}
 * @param volume              multiplier applied on top of the base jukebox
 *                            volume of {@code 4.0} (default {@code 1.0}).
 *                            Higher values increase the effective broadcast
 *                            range
 * @param pitch               playback pitch (default {@code 1.0})
 * @param stream              stream the file from disk rather than
 *                            preloading it (default {@code true}). Keep
 *                            this enabled for any sound longer than a few
 *                            seconds
 * @param attenuationDistance base distance for 3D falloff (default
 *                            {@code 16}). The effective range is
 *                            {@code max(volume, 1.0) * 4.0 * attenuationDistance}
 * @see BackstubeMusicDisc
 * @since 0.1.2
 */
public record DiscSound(
        Optional<ResourceLocation> name,
        float volume,
        float pitch,
        boolean stream,
        int attenuationDistance
) {
    /**
     * The default playback configuration.
     * <p>
     * Equivalent to {@code new DiscSound(Optional.empty(), 1.0F, 1.0F, true,
     * 16)}; used when a disc does not provide a {@code sound} value of its own.
     *
     * @since 0.1.2
     */
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

    /**
     * Codec accepting either a {@link ResourceLocation} string (location-only
     * shortcut) or a full JSON object with all fields.
     * <p>
     * The string form is equivalent to providing only {@code name}; every other
     * field is left at its default.
     *
     * @since 0.1.2
     */
    public static final Codec<DiscSound> CODEC = Codec.either(STRING_CODEC, OBJECT_CODEC)
            .xmap(e -> e.map(s -> s, o -> o), Either::right);

    /**
     * Returns the effective audio sound id for a disc, applying the default if
     * {@link #name} is empty.
     * <p>
     * When {@code name} is present, that value is returned verbatim. Otherwise
     * the id is built from the disc id by keeping its namespace and prefixing
     * its path with {@code records/}: a disc id of
     * {@code example:cool_song} resolves to {@code example:records/cool_song}.
     * Backstube reads the actual OGG from
     * {@code assets/<namespace>/sounds/<rest>.ogg}.
     *
     * @param discId the id of the disc this sound belongs to; must not be
     *               {@code null}
     * @return the resolved sound id; never {@code null}
     * @since 0.1.2
     */
    public ResourceLocation resolveName(ResourceLocation discId) {
        return name.orElseGet(() -> new ResourceLocation(discId.getNamespace(), "records/" + discId.getPath()));
    }
}
