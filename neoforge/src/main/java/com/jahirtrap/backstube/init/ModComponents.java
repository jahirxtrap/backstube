package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.jahirtrap.backstube.BackstubeMod.MODID;

public class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(MODID);

    public static final Supplier<DataComponentType<Holder<BackstubeMusicDisc>>> DISC = register("disc", () -> DataComponentType.<Holder<BackstubeMusicDisc>>builder().persistent(BackstubeMusicDisc.CODEC).networkSynchronized(BackstubeMusicDisc.STREAM_CODEC).build());

    private static <T> Supplier<DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> supplier) {
        return COMPONENTS.register(name, supplier);
    }

    public static void init(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
