package net.blueberrymc.client.event;

import net.blueberrymc.client.event.render.LiquidBlockRenderEvent;
import net.blueberrymc.client.event.render.gui.OverlayChangedEvent;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ClientEventFactory {
    public static LiquidBlockRenderEvent callLiquidBlockRenderEvent(@NotNull FluidState fluidState, @NotNull BlockPos blockPos, int color) {
        return Blueberry.getEventManager().callEvent(new LiquidBlockRenderEvent(fluidState, blockPos, color));
    }

    public static void callScreenChangedEvent(@Nullable Screen screen) {
        Blueberry.getEventManager().callEvent(new ScreenChangedEvent(screen));
    }

    public static void callOverlayChangedEvent(@Nullable Overlay overlay) {
        Blueberry.getEventManager().callEvent(new OverlayChangedEvent(overlay));
    }

    public static void callPreTextureStitchEvent(@NotNull TextureAtlas textureAtlas, @NotNull Set<ResourceLocation> sprites) {
        Blueberry.getEventManager().callEvent(new TextureStitchEvent.Pre(textureAtlas, sprites));
    }
}
