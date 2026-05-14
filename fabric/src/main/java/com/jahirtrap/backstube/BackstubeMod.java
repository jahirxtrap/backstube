package com.jahirtrap.backstube;

import com.jahirtrap.configlib.TXFConfig;
import com.jahirtrap.backstube.init.ModConfig;
import net.fabricmc.api.ModInitializer;

public class BackstubeMod implements ModInitializer {

    public static final String MODID = "backstube";

    @Override
    public void onInitialize() {
        TXFConfig.init(MODID, ModConfig.class);
    }
}
