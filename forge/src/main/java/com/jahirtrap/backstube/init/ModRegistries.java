package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraftforge.registries.DataPackRegistryEvent;

public class ModRegistries {
    public static void init() {
        DataPackRegistryEvent.NewRegistry.BUS.addListener(event ->
                event.dataPackRegistry(BackstubeMusicDisc.REGISTRY_KEY, BackstubeMusicDisc.DIRECT_CODEC, BackstubeMusicDisc.DIRECT_CODEC));
    }
}
