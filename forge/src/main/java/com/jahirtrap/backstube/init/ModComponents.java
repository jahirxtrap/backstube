package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final RegistryObject<DataComponentType<Holder<BackstubeMusicDisc>>> DISC = COMPONENTS.register("disc", () -> DataComponentType.<Holder<BackstubeMusicDisc>>builder().persistent(BackstubeMusicDisc.CODEC).networkSynchronized(BackstubeMusicDisc.STREAM_CODEC).build());

    public static void init(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
