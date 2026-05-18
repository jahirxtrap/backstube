package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModTab {
    public static final CreativeModeTab TAB_BACKSTUBE = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModContent.MUSIC_DISC))
            .displayItems((features, event) -> BackstubeAPI.getAllDiscs(features.holders())
                    .forEach(disc -> event.accept(BackstubeAPI.discStack(disc))))
            .title(Component.translatable("itemGroup.backstube.tab_backstube"))
            .build();

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(MODID, "tab_backstube"), TAB_BACKSTUBE);
    }
}
