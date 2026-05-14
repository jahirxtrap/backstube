package com.jahirtrap.backstube;

import com.jahirtrap.configlib.TXFConfig;
import com.jahirtrap.backstube.init.ModConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BackstubeMod.MODID)
public class BackstubeMod {

    public static final String MODID = "backstube";

    public BackstubeMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TXFConfig.init(MODID, ModConfig.class);
    }
}
