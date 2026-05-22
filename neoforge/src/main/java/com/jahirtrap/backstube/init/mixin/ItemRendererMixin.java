package com.jahirtrap.backstube.init.mixin;

import com.jahirtrap.backstube.api.BackstubeAPI;
import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.client.BackstubeModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!BackstubeAPI.isDisc(stack)) return;
        BackstubeMusicDisc disc = BackstubeAPI.readDisc(stack).map(Holder::value).orElse(null);
        if (disc == null || disc.model().isEmpty()) return;
        ResourceLocation modelLoc = BackstubeModelLoader.toItemModel(disc.model().get());
        ModelManager mm = Minecraft.getInstance().getModelManager();
        BakedModel model = mm.getModel(new ModelResourceLocation(modelLoc, "standalone"));
        if (model != null && model != mm.getMissingModel()) {
            cir.setReturnValue(model);
        }
    }
}
