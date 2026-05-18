package com.jahirtrap.backstube.sound;

import com.jahirtrap.backstube.api.DiscSound;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.phys.Vec3;

public class BackstubeDiscSoundInstance extends AbstractSoundInstance {
    private final boolean stream;
    private final int discAttenuation;

    public BackstubeDiscSoundInstance(ResourceLocation soundLocation, DiscSound config, Vec3 pos) {
        super(soundLocation, SoundSource.RECORDS, RandomSource.create());
        this.volume = 4.0F * config.volume();
        this.pitch = config.pitch();
        this.attenuation = Attenuation.LINEAR;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.stream = config.stream();
        this.discAttenuation = config.attenuationDistance();
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager soundManager) {
        this.sound = new Sound(this.location.toString(), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, this.stream, false, this.discAttenuation);
        WeighedSoundEvents events = new WeighedSoundEvents(this.location, null);
        events.addSound(this.sound);
        return events;
    }
}
