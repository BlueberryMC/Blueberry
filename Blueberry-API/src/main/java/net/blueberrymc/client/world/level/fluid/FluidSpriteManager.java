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

import java.util.HashMap;
import java.util.Map;

public class FluidSpriteManager {
    private static final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private static final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private static final Map<Fluid, Material> map = new HashMap<>();
    private static boolean init = false;

    public static void add(@NotNull("fluid") Fluid fluid, @NotNull String namespace, @NotNull String path) {
        ResourceLocation resourceLocation = new ResourceLocation(namespace, path);
        Material material = new Material(InventoryMenu.BLOCK_ATLAS, resourceLocation); // TextureAtlas.LOCATION_BLOCKS
        map.put(fluid, material);
    }

    public static Material get(@NotNull("fluid") Fluid fluid) {
        return map.get(fluid);
    }

    private static void init() {
        if (init) return;
        lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        init = true;
    }

    @NotNull
    public static TextureAtlasSprite[] getSprites(@NotNull FluidState fluidState) {
        init();
        if (fluidState.is(FluidTags.LAVA)) return lavaIcons;
        if (fluidState.is(FluidTags.WATER)) return waterIcons;
        try {
            TextureAtlasSprite[] sprites = new TextureAtlasSprite[2];
            sprites[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(fluidState.createLegacyBlock()).getParticleIcon();
            sprites[1] = get(fluidState.getType()).sprite();
            return sprites;
        } catch (RuntimeException ex) { // ignore
            return waterIcons;
        }
    }
}
