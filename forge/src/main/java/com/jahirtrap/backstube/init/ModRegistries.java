package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;

public class ModRegistries {
    public static void init(IEventBus bus) {
        bus.addListener((DataPackRegistryEvent.NewRegistry event) ->
                event.dataPackRegistry(BackstubeMusicDisc.REGISTRY_KEY, BackstubeMusicDisc.DIRECT_CODEC, BackstubeMusicDisc.DIRECT_CODEC));
    }
}
