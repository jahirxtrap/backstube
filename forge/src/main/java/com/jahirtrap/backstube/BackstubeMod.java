package com.jahirtrap.backstube;

import com.jahirtrap.backstube.init.*;
import com.jahirtrap.configlib.TXFConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BackstubeMod.MODID)
public class BackstubeMod {

    public static final String MODID = "backstube";

    public BackstubeMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        TXFConfig.init(MODID, ModConfig.class);
        ModRegistries.init(bus);
        ModComponents.init(bus);
        ModContent.init(bus);
        ModTab.init(bus);
    }
}
