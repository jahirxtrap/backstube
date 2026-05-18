package com.jahirtrap.backstube;

import com.jahirtrap.backstube.init.ModConfig;
import com.jahirtrap.backstube.init.ModContent;
import com.jahirtrap.backstube.init.ModRegistries;
import com.jahirtrap.backstube.init.ModTab;
import com.jahirtrap.configlib.TXFConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BackstubeMod.MODID)
public class BackstubeMod {

    public static final String MODID = "backstube";
    public static final int LEVEL_EVENT_PLAY_DISC = 30010;

    public BackstubeMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TXFConfig.init(MODID, ModConfig.class);
        ModRegistries.init(bus);
        ModContent.init(bus);
        ModTab.init(bus);
    }
}
