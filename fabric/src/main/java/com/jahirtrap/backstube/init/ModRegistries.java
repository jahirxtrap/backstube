package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public class ModRegistries {
    public static void init() {
        DynamicRegistries.registerSynced(BackstubeMusicDisc.REGISTRY_KEY, BackstubeMusicDisc.DIRECT_CODEC);
    }
}
