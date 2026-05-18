package com.jahirtrap.backstube;

import com.jahirtrap.backstube.client.BackstubeModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BackstubeMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BackstubeClient {

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation id : BackstubeModelLoader.findDiscModels(Minecraft.getInstance().getResourceManager())) {
            event.register(ModelResourceLocation.inventory(id));
        }
    }
}
