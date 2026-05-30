package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.jahirtrap.tooltips.init.ModTooltips")
public abstract class TooltipsCompatMixin {

    @Inject(method = "getSongLengthInSeconds", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getSongLengthInSeconds(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if (!BackstubeAPI.isDisc(stack)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null)
            BackstubeAPI.readDisc(stack, mc.level).ifPresent(disc -> cir.setReturnValue(disc.value().lengthInSeconds()));
    }
}
