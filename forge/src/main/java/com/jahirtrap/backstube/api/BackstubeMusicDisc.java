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
import net.minecraft.world.item.Rarity;

import java.util.Locale;
import java.util.Optional;

public record BackstubeMusicDisc(
        Component title,
        Component artist,
        float lengthInSeconds,
        int comparatorOutput,
        Rarity rarity,
        Optional<ResourceLocation> model,
        Optional<DiscSound> sound
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
            DiscSound.CODEC.optionalFieldOf("sound").forGetter(BackstubeMusicDisc::sound)
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
}
