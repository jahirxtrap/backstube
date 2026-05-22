package com.jahirtrap.backstube.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable description of a Backstube music disc.
 * <p>
 * Each instance is the value of one entry in the
 * {@code backstube:music_disc} registry. Discs can be loaded from JSON files
 * under {@code data/<namespace>/backstube/music_disc/<path>.json}, registered
 * from code via {@link BackstubeAPI#createDisc(ResourceLocation, BackstubeMusicDisc)}
 * or both.
 * <p>
 * Use {@link #builder()} to construct an instance fluently; only
 * {@code title}, {@code artist} and {@code lengthInSeconds} are mandatory.
 *
 * @param title            the disc title; shown in tooltips and in the
 *                         {@code Now Playing} message
 * @param artist           the song author
 * @param lengthInSeconds  song duration; must be {@code > 0} and should
 *                         match the OGG file's actual length
 * @param comparatorOutput redstone comparator strength when the disc is in
 *                         a jukebox; must lie in {@code [0, 15]} (default
 *                         {@code 1})
 * @param rarity           the {@link Rarity} of the item tooltip (default
 *                         {@link Rarity#RARE})
 * @param model            optional custom item model id; when absent the
 *                         generic Backstube model is used
 * @param sound            optional {@link DiscSound} overriding the audio
 *                         source and playback parameters
 * @param stackSize        max stack size, in {@code [1, 64]} (default
 *                         {@code 1})
 * @param item             optional custom item id; when absent the generic
 *                         {@code backstube:music_disc} item is used
 * @see BackstubeAPI
 * @see DiscSound
 * @since 0.1.2
 */
public record BackstubeMusicDisc(
        Component title,
        Component artist,
        float lengthInSeconds,
        int comparatorOutput,
        Rarity rarity,
        Optional<ResourceLocation> model,
        Optional<DiscSound> sound,
        int stackSize,
        Optional<ResourceLocation> item
) {
    /**
     * Registry key for the {@code backstube:music_disc} data registry.
     * <p>
     * Mirrors {@link BackstubeAPI#discRegistryKey()} for convenience &mdash; use
     * whichever reads better at the call site.
     *
     * @since 0.1.2
     */
    public static final ResourceKey<Registry<BackstubeMusicDisc>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("backstube", "music_disc"));

    /**
     * Codec that serialises a full {@link BackstubeMusicDisc} as a JSON object.
     * <p>
     * The format matches the on-disk schema used by data-driven discs (see the
     * package documentation for field reference). Used by the registry to load
     * JSON files; also handy when writing custom serialisers (for example, a
     * datapack-generated set of discs).
     *
     * @since 0.1.2
     */
    public static final Codec<BackstubeMusicDisc> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            ComponentSerialization.CODEC.fieldOf("title").forGetter(BackstubeMusicDisc::title),
            ComponentSerialization.CODEC.fieldOf("artist").forGetter(BackstubeMusicDisc::artist),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds").forGetter(BackstubeMusicDisc::lengthInSeconds),
            ExtraCodecs.intRange(0, 15).optionalFieldOf("comparator_output", 1).forGetter(BackstubeMusicDisc::comparatorOutput),
            Rarity.CODEC.optionalFieldOf("rarity", Rarity.RARE).forGetter(BackstubeMusicDisc::rarity),
            ResourceLocation.CODEC.optionalFieldOf("model").forGetter(BackstubeMusicDisc::model),
            DiscSound.CODEC.optionalFieldOf("sound").forGetter(BackstubeMusicDisc::sound),
            ExtraCodecs.intRange(1, Item.DEFAULT_MAX_STACK_SIZE).optionalFieldOf("stack_size", 1).forGetter(BackstubeMusicDisc::stackSize),
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(BackstubeMusicDisc::item)
    ).apply(i, BackstubeMusicDisc::new));

    private static final StreamCodec<ByteBuf, Optional<ResourceLocation>> OPTIONAL_ID_STREAM_CODEC =
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC);

    private static final StreamCodec<ByteBuf, DiscSound> DISC_SOUND_STREAM_CODEC = StreamCodec.composite(
            OPTIONAL_ID_STREAM_CODEC, DiscSound::name,
            ByteBufCodecs.FLOAT, DiscSound::volume,
            ByteBufCodecs.FLOAT, DiscSound::pitch,
            ByteBufCodecs.BOOL, DiscSound::stream,
            ByteBufCodecs.VAR_INT, DiscSound::attenuationDistance,
            DiscSound::new
    );

    private static final StreamCodec<ByteBuf, Optional<DiscSound>> OPTIONAL_SOUND_STREAM_CODEC =
            ByteBufCodecs.optional(DISC_SOUND_STREAM_CODEC);

    /**
     * Stream codec that encodes a full {@link BackstubeMusicDisc} over the
     * network.
     * <p>
     * Used by Backstube to sync the data registry from server to client. Useful
     * for custom payload packets that need to ship a complete disc value rather
     * than a registry reference.
     *
     * @see #STREAM_CODEC
     * @since 0.1.2
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, BackstubeMusicDisc> DIRECT_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BackstubeMusicDisc decode(RegistryFriendlyByteBuf buf) {
            return new BackstubeMusicDisc(
                    ComponentSerialization.STREAM_CODEC.decode(buf),
                    ComponentSerialization.STREAM_CODEC.decode(buf),
                    buf.readFloat(),
                    buf.readVarInt(),
                    Rarity.STREAM_CODEC.decode(buf),
                    OPTIONAL_ID_STREAM_CODEC.decode(buf),
                    OPTIONAL_SOUND_STREAM_CODEC.decode(buf),
                    buf.readVarInt(),
                    OPTIONAL_ID_STREAM_CODEC.decode(buf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BackstubeMusicDisc value) {
            ComponentSerialization.STREAM_CODEC.encode(buf, value.title);
            ComponentSerialization.STREAM_CODEC.encode(buf, value.artist);
            buf.writeFloat(value.lengthInSeconds);
            buf.writeVarInt(value.comparatorOutput);
            Rarity.STREAM_CODEC.encode(buf, value.rarity);
            OPTIONAL_ID_STREAM_CODEC.encode(buf, value.model);
            OPTIONAL_SOUND_STREAM_CODEC.encode(buf, value.sound);
            buf.writeVarInt(value.stackSize);
            OPTIONAL_ID_STREAM_CODEC.encode(buf, value.item);
        }
    };

    /**
     * Codec for a {@link Holder} reference to a registered disc.
     * <p>
     * Prefer this over {@link #DIRECT_CODEC} when authoring custom data
     * components, recipes, predicates or any payload that should reference an
     * existing registry entry by id rather than embedding the whole disc value.
     *
     * @since 0.1.2
     */
    public static final Codec<Holder<BackstubeMusicDisc>> CODEC = RegistryFixedCodec.create(REGISTRY_KEY);

    /**
     * Stream codec for a {@link Holder} reference to a registered disc.
     * <p>
     * Counterpart to {@link #CODEC} for network sync; encodes the disc by
     * registry id, falling back to the full value when the receiving side does
     * not have the entry.
     *
     * @see #CODEC
     * @since 0.1.2
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<BackstubeMusicDisc>> STREAM_CODEC =
            ByteBufCodecs.holder(REGISTRY_KEY, DIRECT_STREAM_CODEC);

    /**
     * Returns the song duration in game ticks.
     * <p>
     * Computed as {@code ceil(lengthInSeconds * 20)}; convenient when scheduling
     * tick-based logic against the song.
     *
     * @return the song length expressed in ticks; always {@code > 0}
     * @since 0.1.2
     */
    public int lengthInTicks() {
        return Mth.ceil(this.lengthInSeconds * 20.0F);
    }

    /**
     * Returns whether the song has played to completion, given how many ticks
     * have elapsed since playback started.
     * <p>
     * Uses a 20-tick grace period (one second) past {@link #lengthInTicks()} so
     * that short rounding errors at the end of the OGG do not trigger an early
     * stop.
     *
     * @param ticksElapsed ticks since the jukebox started playing this disc
     * @return {@code true} when {@code ticksElapsed >= lengthInTicks() + 20}
     * @since 0.1.2
     */
    public boolean hasFinished(long ticksElapsed) {
        return ticksElapsed >= this.lengthInTicks() + 20;
    }

    /**
     * Returns the formatted "Artist - Title" component used as the disc's
     * tooltip subtitle and {@code Now Playing} message.
     * <p>
     * The result is a fresh component constructed by concatenating
     * {@link #artist()}, the literal {@code " - "} separator and {@link #title()}.
     *
     * @return a non-{@code null} component; never empty
     * @since 0.1.2
     */
    public Component description() {
        return Component.empty().append(this.artist).append(" - ").append(this.title);
    }

    /**
     * Returns a new {@link Builder} for fluently constructing a
     * {@link BackstubeMusicDisc}.
     * <p>
     * The builder pre-fills sensible defaults for every optional component; only
     * {@link Builder#title}, {@link Builder#artist} and
     * {@link Builder#lengthInSeconds} are required before calling
     * {@link Builder#build()}.
     *
     * @return a fresh builder instance; never {@code null}
     * @since 0.1.3
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link BackstubeMusicDisc}.
     * <p>
     * Mandatory fields are {@code title}, {@code artist} and
     * {@code lengthInSeconds}; all others fall back to documented defaults if
     * left unset. Reuse a single builder for multiple discs by overriding fields
     * between {@code build()} calls; the builder is mutable but not
     * thread-safe.
     *
     * @since 0.1.3
     */
    public static final class Builder {
        private Component title;
        private Component artist;
        private float lengthInSeconds = -1F;
        private int comparatorOutput = 1;
        private Rarity rarity = Rarity.RARE;
        private ResourceLocation model;
        private DiscSound sound;
        private int stackSize = 1;
        private ResourceLocation item;

        private Builder() {
        }

        /**
         * Sets the disc title.
         * <p>
         * The {@code String} overload wraps the value in
         * {@link Component#literal(String)}.
         *
         * @param title the title text
         * @return this builder
         * @since 0.1.3
         */
        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the disc title.
         * <p>
         * The {@code String} overload wraps the value in
         * {@link Component#literal(String)}.
         *
         * @param literal the title text
         * @return this builder
         * @since 0.1.3
         */
        public Builder title(String literal) {
            return title(Component.literal(literal));
        }

        /**
         * Sets the song artist.
         * <p>
         * The {@code String} overload wraps the value in
         * {@link Component#literal(String)}.
         *
         * @param artist the artist text
         * @return this builder
         * @since 0.1.3
         */
        public Builder artist(Component artist) {
            this.artist = artist;
            return this;
        }

        /**
         * Sets the song artist.
         * <p>
         * The {@code String} overload wraps the value in
         * {@link Component#literal(String)}.
         *
         * @param literal the artist text
         * @return this builder
         * @since 0.1.3
         */
        public Builder artist(String literal) {
            return artist(Component.literal(literal));
        }

        /**
         * Sets the song duration in seconds.
         * <p>
         * Must match the OGG file's actual length to within a tick or playback may
         * be cut short or looped incorrectly. The value passed to {@link #build()}
         * must be strictly greater than zero.
         *
         * @param seconds the duration
         * @return this builder
         * @since 0.1.3
         */
        public Builder lengthInSeconds(float seconds) {
            this.lengthInSeconds = seconds;
            return this;
        }

        /**
         * Sets the redstone comparator output strength when the disc is in a
         * jukebox.
         * <p>
         * Must be in the range {@code [0, 15]} (validated by the codec when the
         * value is serialised). Defaults to {@code 1}.
         *
         * @param comparatorOutput the comparator strength
         * @return this builder
         * @since 0.1.3
         */
        public Builder comparatorOutput(int comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        /**
         * Sets the {@link Rarity} used for the disc's item tooltip colour.
         * <p>
         * Defaults to {@link Rarity#RARE}.
         *
         * @param rarity the rarity
         * @return this builder
         * @since 0.1.3
         */
        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * Sets a custom item-model id used by the rendered disc.
         * <p>
         * The id is resolved through the item-model registry, i.e. it must point at
         * an entry under {@code assets/<namespace>/items/<path>.json}. Leaving this
         * unset uses the generic Backstube model.
         *
         * @param model the model id
         * @return this builder
         * @since 0.1.3
         */
        public Builder model(ResourceLocation model) {
            this.model = model;
            return this;
        }

        /**
         * Overrides the audio source and/or playback parameters.
         * <p>
         * The {@link ResourceLocation} overload is a shortcut that constructs a
         * {@link DiscSound} with the supplied name and all other parameters set to
         * their defaults ({@code volume=1.0}, {@code pitch=1.0}, {@code stream=true},
         * {@code attenuationDistance=16}).
         *
         * @param sound the sound configuration
         * @return this builder
         * @see DiscSound
         * @since 0.1.3
         */
        public Builder sound(DiscSound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Overrides the audio source and/or playback parameters.
         * <p>
         * The {@link ResourceLocation} overload is a shortcut that constructs a
         * {@link DiscSound} with the supplied name and all other parameters set to
         * their defaults ({@code volume=1.0}, {@code pitch=1.0}, {@code stream=true},
         * {@code attenuationDistance=16}).
         *
         * @param soundLocation the sound configuration
         * @return this builder
         * @see DiscSound
         * @since 0.1.3
         */
        public Builder sound(ResourceLocation soundLocation) {
            return sound(new DiscSound(Optional.of(soundLocation), 1F, 1F, true, 16));
        }

        /**
         * Sets the maximum stack size for the disc item.
         * <p>
         * Must be in the range {@code [1, 64]} (validated by the codec when the
         * value is serialised). Defaults to {@code 1}, matching vanilla music discs.
         *
         * @param stackSize the max stack size
         * @return this builder
         * @since 0.1.3
         */
        public Builder stackSize(int stackSize) {
            this.stackSize = stackSize;
            return this;
        }

        /**
         * Binds the disc data to a specific item id.
         * <p>
         * When set, {@link BackstubeAPI#discStack} and tooltips will use the given
         * item instead of the generic {@code backstube:music_disc}. The item must
         * exist at runtime; if it is missing, Backstube falls back to the generic
         * item.
         * <p>
         * Note that {@link BackstubeAPI#createDisc(ResourceLocation, BackstubeMusicDisc)}
         * already auto-fills this field with the registration id.
         *
         * @param item the item id to bind to
         * @return this builder
         * @since 0.1.3
         */
        public Builder item(ResourceLocation item) {
            this.item = item;
            return this;
        }

        /**
         * Builds the immutable {@link BackstubeMusicDisc} instance.
         *
         * @return the constructed disc
         * @throws NullPointerException  if {@code title} or {@code artist} was not
         *                               set
         * @throws IllegalStateException if {@code lengthInSeconds} was not set or
         *                               is not strictly positive
         * @since 0.1.3
         */
        public BackstubeMusicDisc build() {
            Objects.requireNonNull(title, "title is required");
            Objects.requireNonNull(artist, "artist is required");
            if (lengthInSeconds <= 0F) throw new IllegalStateException("lengthInSeconds must be > 0");
            return new BackstubeMusicDisc(title, artist, lengthInSeconds, comparatorOutput, rarity,
                    Optional.ofNullable(model), Optional.ofNullable(sound), stackSize, Optional.ofNullable(item));
        }
    }
}
