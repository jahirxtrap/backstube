package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.init.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxSongPlayer.class)
public abstract class JukeboxSongPlayerMixin {

    @Shadow
    private long ticksSinceSongStarted;
    @Shadow
    private @Nullable Holder<JukeboxSong> song;
    @Shadow
    @Final
    private BlockPos blockPos;

    @Unique
    private int timesLooped = 0;

    @Inject(method = "play", at = @At("HEAD"))
    private void play(LevelAccessor level, Holder<JukeboxSong> song, CallbackInfo ci) {
        this.timesLooped = 0;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(LevelAccessor level, BlockState blockState, CallbackInfo ci) {
        if (level.isClientSide()) return;
        if (this.song == null || !this.song.value().hasFinished(this.ticksSinceSongStarted)) return;
        boolean infinite = ModConfig.jukeboxLoopInfinite;
        int max = ModConfig.jukeboxLoopCount;
        if (!infinite && this.timesLooped >= max) return;
        if (!infinite) this.timesLooped++;
        this.ticksSinceSongStarted = 0L;
        int songId = level.registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).getId(this.song.value());
        level.levelEvent(null, 1010, this.blockPos, songId);
        ci.cancel();
    }
}
