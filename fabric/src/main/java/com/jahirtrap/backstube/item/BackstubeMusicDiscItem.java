package com.jahirtrap.backstube.item;

import com.jahirtrap.backstube.api.BackstubeAPI;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;

public class BackstubeMusicDiscItem extends RecordItem {
    public BackstubeMusicDiscItem(SoundEvent placeholderSound, Properties properties) {
        super(1, placeholderSound, properties, 0);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return BackstubeAPI.readRarity(stack);
    }
}
