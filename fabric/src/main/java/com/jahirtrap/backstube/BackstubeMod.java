package com.jahirtrap.backstube;

import com.jahirtrap.backstube.init.ModConfig;
import com.jahirtrap.backstube.init.ModContent;
import com.jahirtrap.backstube.init.ModRegistries;
import com.jahirtrap.backstube.init.ModTab;
import com.jahirtrap.configlib.TXFConfig;
import net.fabricmc.api.ModInitializer;

public class BackstubeMod implements ModInitializer {

    public static final String MODID = "backstube";
    public static final int LEVEL_EVENT_PLAY_DISC = 30010;

    @Override
    public void onInitialize() {
        TXFConfig.init(MODID, ModConfig.class);
        ModRegistries.init();
        ModContent.init();
        ModTab.init();
    }
}
