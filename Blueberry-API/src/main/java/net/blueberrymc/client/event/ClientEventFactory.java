package net.blueberrymc.client.event;

import net.blueberrymc.client.event.render.gui.OverlayChangedEvent;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@ApiStatus.Internal
public class ClientEventFactory {
    public static void callScreenChangedEvent(@Nullable Screen screen) {
        new ScreenChangedEvent(screen).callEvent();
    }

    public static void callOverlayChangedEvent(@Nullable Overlay overlay) {
        new OverlayChangedEvent(overlay).callEvent();
    }

    public static void callPreTextureStitchEvent(@NotNull TextureAtlas textureAtlas, @NotNull Set<Identifier> sprites) {
        new TextureStitchEvent.Pre(textureAtlas, sprites).callEvent();
    }
}
