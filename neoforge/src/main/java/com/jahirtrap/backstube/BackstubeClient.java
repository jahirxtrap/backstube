package com.jahirtrap.backstube;

import com.jahirtrap.backstube.client.BackstubeModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = BackstubeMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BackstubeClient {

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation id : BackstubeModelLoader.findDiscModels(Minecraft.getInstance().getResourceManager())) {
            event.register(new ModelResourceLocation(id, "standalone"));
        }
    }
}
