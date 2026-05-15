package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class ModRegistries {
    public static void init(IEventBus bus) {
        bus.addListener(DataPackRegistryEvent.NewRegistry.class, event ->
                event.dataPackRegistry(BackstubeMusicDisc.REGISTRY_KEY, BackstubeMusicDisc.DIRECT_CODEC, BackstubeMusicDisc.DIRECT_CODEC));
    }
}
