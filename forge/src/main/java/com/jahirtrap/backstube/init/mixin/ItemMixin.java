package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    private void getRarity(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        if (!BackstubeAPI.isDisc(stack)) return;
        cir.setReturnValue(BackstubeAPI.readRarity(stack));
    }
}
