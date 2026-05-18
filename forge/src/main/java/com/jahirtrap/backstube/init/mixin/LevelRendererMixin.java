package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.BackstubeMod;
import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.api.DiscSound;
import com.jahirtrap.backstube.sound.BackstubeDiscSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private Map<BlockPos, SoundInstance> playingRecords;

    @Shadow
    protected abstract void notifyNearbyEntities(Level level, BlockPos pos, boolean playing);

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void levelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId != BackstubeMod.LEVEL_EVENT_PLAY_DISC) return;
        if (this.level == null) return;

        Registry<BackstubeMusicDisc> registry = this.level.registryAccess().registryOrThrow(BackstubeMusicDisc.REGISTRY_KEY);
        Holder.Reference<BackstubeMusicDisc> holder = registry.getHolder(data).orElse(null);
        if (holder == null) {
            ci.cancel();
            return;
        }

        BackstubeMusicDisc disc = holder.value();
        ResourceLocation discId = holder.key().location();
        ResourceLocation soundLoc = BackstubeAPI.discSoundLocation(disc, discId);
        DiscSound config = disc.sound().orElse(DiscSound.DEFAULT);

        Minecraft mc = Minecraft.getInstance();
        SoundInstance existing = this.playingRecords.get(pos);
        if (existing != null) {
            mc.getSoundManager().stop(existing);
            this.playingRecords.remove(pos);
        }

        SoundInstance instance = new BackstubeDiscSoundInstance(soundLoc, config, Vec3.atCenterOf(pos));
        this.playingRecords.put(pos, instance);
        mc.getSoundManager().play(instance);
        mc.gui.setNowPlaying(disc.description());
        this.notifyNearbyEntities(this.level, pos, true);

        ci.cancel();
    }
}
