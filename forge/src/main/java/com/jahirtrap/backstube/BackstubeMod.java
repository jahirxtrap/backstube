package com.jahirtrap.backstube;

import com.jahirtrap.backstube.init.*;
import com.jahirtrap.configlib.TXFConfig;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BackstubeMod.MODID)
public class BackstubeMod {

    public static final String MODID = "backstube";

    public BackstubeMod(FMLJavaModLoadingContext context) {
        BusGroup bus = context.getModBusGroup();

        TXFConfig.init(MODID, ModConfig.class);
        ModRegistries.init();
        ModComponents.init(bus);
        ModContent.init(bus);
        ModTab.init(bus);
    }
}
