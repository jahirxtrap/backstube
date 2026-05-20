package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.BackstubeMod;
import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.init.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {

    @Shadow
    private long tickCount;
    @Shadow
    private long recordStartedTick;
    @Shadow
    private boolean isPlaying;

    @Unique
    private int timesLooped = 0;

    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void startPlaying(CallbackInfo ci) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        Level level = jukebox.getLevel();
        if (level == null) return;
        ItemStack stack = jukebox.getFirstItem();
        Holder<BackstubeMusicDisc> holder = BackstubeAPI.readDisc(stack, level).orElse(null);
        if (holder == null) return;

        BlockPos pos = jukebox.getBlockPos();
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.timesLooped = 0;
        level.updateNeighborsAt(pos, jukebox.getBlockState().getBlock());
        level.levelEvent(null, BackstubeMod.LEVEL_EVENT_PLAY_DISC, pos, level.registryAccess().registryOrThrow(BackstubeMusicDisc.REGISTRY_KEY).getId(holder.value()));
        jukebox.setChanged();
        ci.cancel();
    }

    @Inject(method = "shouldRecordStopPlaying", at = @At("HEAD"), cancellable = true)
    private void shouldRecordStopPlaying(RecordItem record, CallbackInfoReturnable<Boolean> cir) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        Level level = jukebox.getLevel();
        if (level == null) return;
        Holder<BackstubeMusicDisc> holder = BackstubeAPI.readDisc(jukebox.getFirstItem(), level).orElse(null);
        if (holder == null) return;
        BackstubeMusicDisc disc = holder.value();

        boolean finished = this.tickCount >= this.recordStartedTick + disc.lengthInTicks() + 20L;
        if (!finished) {
            cir.setReturnValue(false);
            return;
        }
        boolean infinite = ModConfig.jukeboxLoopInfinite;
        int max = ModConfig.jukeboxLoopCount;
        if (!infinite && this.timesLooped >= max) {
            cir.setReturnValue(true);
            return;
        }
        if (!infinite) this.timesLooped++;
        this.recordStartedTick = this.tickCount;
        level.levelEvent(null, BackstubeMod.LEVEL_EVENT_PLAY_DISC, jukebox.getBlockPos(), level.registryAccess().registryOrThrow(BackstubeMusicDisc.REGISTRY_KEY).getId(disc));
        cir.setReturnValue(false);
    }
}
