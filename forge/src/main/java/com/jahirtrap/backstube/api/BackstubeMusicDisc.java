package com.jahirtrap.backstube.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
    public static final ResourceKey<Registry<BackstubeMusicDisc>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation("backstube", "music_disc"));

    public static final Codec<Rarity> RARITY_CODEC = Codec.STRING.flatXmap(
            name -> {
                try {
                    return DataResult.success(Rarity.valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown rarity: " + name);
                }
            },
            r -> DataResult.success(r.name().toLowerCase(Locale.ROOT))
    );

    public static final Codec<BackstubeMusicDisc> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.COMPONENT.fieldOf("title").forGetter(BackstubeMusicDisc::title),
            ExtraCodecs.COMPONENT.fieldOf("artist").forGetter(BackstubeMusicDisc::artist),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds").forGetter(BackstubeMusicDisc::lengthInSeconds),
            ExtraCodecs.intRange(0, 15).optionalFieldOf("comparator_output", 1).forGetter(BackstubeMusicDisc::comparatorOutput),
            RARITY_CODEC.optionalFieldOf("rarity", Rarity.RARE).forGetter(BackstubeMusicDisc::rarity),
            ResourceLocation.CODEC.optionalFieldOf("model").forGetter(BackstubeMusicDisc::model),
            DiscSound.CODEC.optionalFieldOf("sound").forGetter(BackstubeMusicDisc::sound),
            ExtraCodecs.intRange(1, Item.MAX_STACK_SIZE).optionalFieldOf("stack_size", 1).forGetter(BackstubeMusicDisc::stackSize),
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(BackstubeMusicDisc::item)
    ).apply(i, BackstubeMusicDisc::new));

    public static final Codec<Holder<BackstubeMusicDisc>> CODEC = RegistryFixedCodec.create(REGISTRY_KEY);

    public int lengthInTicks() {
        return Mth.ceil(this.lengthInSeconds * 20.0F);
    }

    public boolean hasFinished(long ticksElapsed) {
        return ticksElapsed >= this.lengthInTicks() + 20;
    }

    public Component description() {
        return Component.empty().append(this.artist).append(" - ").append(this.title);
    }

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder title(String literal) {
            return title(Component.literal(literal));
        }

        public Builder artist(Component artist) {
            this.artist = artist;
            return this;
        }

        public Builder artist(String literal) {
            return artist(Component.literal(literal));
        }

        public Builder lengthInSeconds(float seconds) {
            this.lengthInSeconds = seconds;
            return this;
        }

        public Builder comparatorOutput(int comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder model(ResourceLocation model) {
            this.model = model;
            return this;
        }

        public Builder sound(DiscSound sound) {
            this.sound = sound;
            return this;
        }

        public Builder sound(ResourceLocation soundLocation) {
            return sound(new DiscSound(Optional.of(soundLocation), 1F, 1F, true, 16));
        }

        public Builder stackSize(int stackSize) {
            this.stackSize = stackSize;
            return this;
        }

        public Builder item(ResourceLocation item) {
            this.item = item;
            return this;
        }

        public BackstubeMusicDisc build() {
            Objects.requireNonNull(title, "title is required");
            Objects.requireNonNull(artist, "artist is required");
            if (lengthInSeconds <= 0F) throw new IllegalStateException("lengthInSeconds must be > 0");
            return new BackstubeMusicDisc(title, artist, lengthInSeconds, comparatorOutput, rarity,
                    Optional.ofNullable(model), Optional.ofNullable(sound), stackSize, Optional.ofNullable(item));
        }
    }
}
