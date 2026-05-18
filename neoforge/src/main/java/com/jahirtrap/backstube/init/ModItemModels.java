package com.jahirtrap.backstube.init;

import com.jahirtrap.backstube.itemmodel.BackstubeMusicDiscItemModel;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItemModels {
    public static final Map<Identifier, MapCodec<? extends ItemModel.Unbaked>> TYPES = new LinkedHashMap<>();

    public static final MapCodec<BackstubeMusicDiscItemModel.Unbaked> MUSIC_DISC =
            register(BackstubeMusicDiscItemModel.TYPE_ID, BackstubeMusicDiscItemModel.Unbaked.MAP_CODEC);

    private static <T extends MapCodec<? extends ItemModel.Unbaked>> T register(Identifier id, T codec) {
        TYPES.put(id, codec);
        return codec;
    }
}
