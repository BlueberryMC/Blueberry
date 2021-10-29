package net.blueberrymc.client.world.level.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FluidSpriteManager {
    private static final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private static final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private static final Map<Fluid, Material> map = new HashMap<>();

    public static void add(@NotNull Fluid fluid, @NotNull String namespace, @NotNull String path) {
        ResourceLocation resourceLocation = new ResourceLocation(namespace, path);
        Material material = new Material(InventoryMenu.BLOCK_ATLAS, resourceLocation); // TextureAtlas.LOCATION_BLOCKS
        map.put(fluid, material);
    }

    @Nullable
    public static Material get(@NotNull Fluid fluid) {
        return map.get(fluid);
    }

    public static void setupSprites() {
        lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
    }

    @NotNull
    public static TextureAtlasSprite[] getSprites(@NotNull FluidState fluidState) {
        if (fluidState.is(FluidTags.LAVA)) return lavaIcons;
        if (fluidState.is(FluidTags.WATER)) return waterIcons;
        try {
            TextureAtlasSprite[] sprites = new TextureAtlasSprite[2];
            sprites[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(fluidState.createLegacyBlock()).getParticleIcon();
            sprites[1] = Objects.requireNonNull(get(fluidState.getType())).sprite();
            return sprites;
        } catch (RuntimeException ex) { // ignore
            return waterIcons;
        }
    }
}
