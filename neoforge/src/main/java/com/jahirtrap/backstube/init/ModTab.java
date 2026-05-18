package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> TAB_BACKSTUBE = TABS.register("tab_backstube", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ModContent.MUSIC_DISC.get()))
            .displayItems((features, event) -> BackstubeAPI.getAllDiscs(features.holders())
                    .forEach(disc -> event.accept(BackstubeAPI.discStack(disc, features.holders()))))
            .title(Component.translatable("itemGroup.backstube.tab_backstube"))
            .build());

    public static void init(IEventBus bus) {
        TABS.register(bus);
    }
}
