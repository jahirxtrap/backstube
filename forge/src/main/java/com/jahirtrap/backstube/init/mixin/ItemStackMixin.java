package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int discStackSize = BackstubeAPI.readStackSize((ItemStack) (Object) this);
        if (discStackSize > 0) cir.setReturnValue(discStackSize);
    }
}
