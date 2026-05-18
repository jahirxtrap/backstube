package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModComponents {
    public static final DataComponentType<Holder<BackstubeMusicDisc>> DISC = register("disc", DataComponentType.<Holder<BackstubeMusicDisc>>builder().persistent(BackstubeMusicDisc.CODEC).networkSynchronized(BackstubeMusicDisc.STREAM_CODEC).build());

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> component) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, name), component);
    }

    public static void init() {
    }
}
