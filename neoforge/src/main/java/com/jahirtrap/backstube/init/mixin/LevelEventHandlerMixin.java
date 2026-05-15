package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.api.DiscSound;
import com.jahirtrap.backstube.sound.BackstubeDiscSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(LevelEventHandler.class)
public abstract class LevelEventHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private ClientLevel level;
    @Shadow
    @Final
    private Map<BlockPos, SoundInstance> playingJukeboxSongs;

    @Shadow
    protected abstract void stopJukeboxSong(BlockPos pos);

    @Shadow
    protected abstract void notifyNearbyEntities(Level level, BlockPos pos, boolean isPlaying);

    @Unique
    private static final Identifier BACKSTUBE_DISC_PLACEHOLDER = Identifier.fromNamespaceAndPath("backstube", "disc");

    @Inject(method = "playJukeboxSong", at = @At("HEAD"), cancellable = true)
    private void backstube$overrideJukeboxSound(Holder<JukeboxSong> songHolder, BlockPos pos, CallbackInfo ci) {
        JukeboxSong song = songHolder.value();
        if (!BACKSTUBE_DISC_PLACEHOLDER.equals(song.soundEvent().value().location())) return;
        Optional<ResourceKey<JukeboxSong>> key = songHolder.unwrapKey();
        if (key.isEmpty()) return;
        Identifier discId = key.get().identifier();
        DiscSound config = this.level.registryAccess().lookup(BackstubeMusicDisc.REGISTRY_KEY)
                .flatMap(reg -> reg.get(ResourceKey.create(BackstubeMusicDisc.REGISTRY_KEY, discId)))
                .map(h -> h.value().sound().orElse(DiscSound.DEFAULT))
                .orElse(DiscSound.DEFAULT);
        Identifier soundLocation = config.resolveName(discId);
        this.stopJukeboxSong(pos);
        SoundInstance instance = new BackstubeDiscSoundInstance(soundLocation, config, Vec3.atCenterOf(pos));
        this.playingJukeboxSongs.put(pos, instance);
        this.minecraft.getSoundManager().play(instance);
        this.minecraft.gui.setNowPlaying(song.description());
        this.notifyNearbyEntities(this.level, pos, true);
        ci.cancel();
    }
}
