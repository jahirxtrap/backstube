package com.jahirtrap.backstube;

import com.jahirtrap.backstube.client.BackstubeModelLoader;
import net.fabricmc.api.ClientModInitializer;

public class BackstubeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BackstubeModelLoader.init();
    }
}
