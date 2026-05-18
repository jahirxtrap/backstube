package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.item.BackstubeMusicDiscItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin {

    @Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof JukeboxBlockEntity jb)) return;
        ItemStack stack = jb.getFirstItem();
        if (!(stack.getItem() instanceof BackstubeMusicDiscItem)) return;
        ResourceLocation discId = BackstubeAPI.readDiscId(stack);
        if (discId == null) return;
        BackstubeMusicDisc disc = level.registryAccess().registryOrThrow(BackstubeMusicDisc.REGISTRY_KEY).get(discId);
        if (disc == null) return;
        cir.setReturnValue(disc.comparatorOutput());
    }
}
