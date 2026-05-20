package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecordItem.class)
public abstract class RecordItemMixin {

    @Unique
    private ItemStack stack;

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        this.stack = stack;
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void getDisplayName(CallbackInfoReturnable<MutableComponent> cir) {
        if (this.stack == null) return;
        Component desc = BackstubeAPI.readDescription(this.stack);
        if (desc != null) cir.setReturnValue(desc.copy());
    }
}
