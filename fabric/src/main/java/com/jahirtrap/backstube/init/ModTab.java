package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModTab {
    public static final CreativeModeTab TAB_BACKSTUBE = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ModContent.MUSIC_DISC))
            .displayItems((features, event) -> BackstubeAPI.getAllDiscs(features.holders())
                    .forEach(disc -> event.accept(BackstubeAPI.discStack(disc, features.holders()))))
            .title(Component.translatable("itemGroup.backstube.tab_backstube"))
            .build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MODID, "tab_backstube"), TAB_BACKSTUBE);
    }
}
