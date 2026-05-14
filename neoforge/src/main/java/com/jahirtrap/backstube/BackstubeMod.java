package com.jahirtrap.backstube;

import com.jahirtrap.configlib.TXFConfig;
import com.jahirtrap.backstube.init.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BackstubeMod.MODID)
public class BackstubeMod {

    public static final String MODID = "backstube";

    public BackstubeMod(IEventBus bus) {
        TXFConfig.init(MODID, ModConfig.class);
    }
}
