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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Rarity;

import java.util.Optional;

public record BackstubeMusicDisc(
        Holder<SoundEvent> soundEvent,
        Component title,
        Component artist,
        float lengthInSeconds,
        int comparatorOutput,
        Rarity rarity,
        Optional<Identifier> model
) {
    public static final ResourceKey<Registry<BackstubeMusicDisc>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("backstube", "music_disc"));

    public static final Codec<BackstubeMusicDisc> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            SoundEvent.CODEC.fieldOf("sound_event").forGetter(BackstubeMusicDisc::soundEvent),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(BackstubeMusicDisc::title),
            ComponentSerialization.CODEC.fieldOf("artist").forGetter(BackstubeMusicDisc::artist),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds").forGetter(BackstubeMusicDisc::lengthInSeconds),
            ExtraCodecs.intRange(0, 15).optionalFieldOf("comparator_output", 1).forGetter(BackstubeMusicDisc::comparatorOutput),
            Rarity.CODEC.optionalFieldOf("rarity", Rarity.RARE).forGetter(BackstubeMusicDisc::rarity),
            Identifier.CODEC.optionalFieldOf("model").forGetter(BackstubeMusicDisc::model)
    ).apply(i, BackstubeMusicDisc::new));

    private static final StreamCodec<ByteBuf, Optional<Identifier>> OPTIONAL_ID_STREAM_CODEC =
            ByteBufCodecs.optional(Identifier.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, BackstubeMusicDisc> DIRECT_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BackstubeMusicDisc decode(RegistryFriendlyByteBuf buf) {
            return new BackstubeMusicDisc(
                    SoundEvent.STREAM_CODEC.decode(buf),
                    ComponentSerialization.STREAM_CODEC.decode(buf),
                    ComponentSerialization.STREAM_CODEC.decode(buf),
                    buf.readFloat(),
                    buf.readVarInt(),
                    Rarity.STREAM_CODEC.decode(buf),
                    OPTIONAL_ID_STREAM_CODEC.decode(buf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BackstubeMusicDisc value) {
            SoundEvent.STREAM_CODEC.encode(buf, value.soundEvent);
            ComponentSerialization.STREAM_CODEC.encode(buf, value.title);
            ComponentSerialization.STREAM_CODEC.encode(buf, value.artist);
            buf.writeFloat(value.lengthInSeconds);
            buf.writeVarInt(value.comparatorOutput);
            Rarity.STREAM_CODEC.encode(buf, value.rarity);
            OPTIONAL_ID_STREAM_CODEC.encode(buf, value.model);
        }
    };

    public static final Codec<Holder<BackstubeMusicDisc>> CODEC = RegistryFixedCodec.create(REGISTRY_KEY);

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<BackstubeMusicDisc>> STREAM_CODEC =
            ByteBufCodecs.holder(REGISTRY_KEY, DIRECT_STREAM_CODEC);

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
