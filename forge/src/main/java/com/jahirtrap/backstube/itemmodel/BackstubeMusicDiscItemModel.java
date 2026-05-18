package com.jahirtrap.backstube.itemmodel;

import com.jahirtrap.backstube.api.BackstubeMusicDisc;
import com.jahirtrap.backstube.init.ModComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BackstubeMusicDiscItemModel implements ItemModel {
    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("backstube", "music_disc");

    private final ItemModel fallback;

    public BackstubeMusicDiscItemModel(ItemModel fallback) {
        this.fallback = fallback;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        Holder<BackstubeMusicDisc> disc = item.get(ModComponents.DISC.get());
        if (disc != null && disc.value().model().isPresent() && !TYPE_ID.equals(disc.value().model().get())) {
            Minecraft.getInstance().getModelManager().getItemModel(disc.value().model().get())
                    .update(output, item, resolver, displayContext, level, owner, seed);
        } else {
            this.fallback.update(output, item, resolver, displayContext, level, owner, seed);
        }
    }

    public record Unbaked(ItemModel.Unbaked fallback) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemModels.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext context) {
            return new BackstubeMusicDiscItemModel(this.fallback.bake(context));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.resolveDependencies(resolver);
        }
    }
}
