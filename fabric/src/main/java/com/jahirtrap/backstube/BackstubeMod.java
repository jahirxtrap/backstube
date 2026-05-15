package com.jahirtrap.backstube;

import com.jahirtrap.backstube.init.*;
import com.jahirtrap.configlib.TXFConfig;
import net.fabricmc.api.ModInitializer;

public class BackstubeMod implements ModInitializer {

    public static final String MODID = "backstube";

    @Override
    public void onInitialize() {
        TXFConfig.init(MODID, ModConfig.class);
        ModRegistries.init();
        ModComponents.init();
        ModContent.init();
        ModTab.init();
    }
}
