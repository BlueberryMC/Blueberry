package net.blueberrymc.client.event;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TextureStitchEvent extends Event {
    private final TextureAtlas textureAtlas;

    protected TextureStitchEvent(@NotNull TextureAtlas textureAtlas) {
        this(textureAtlas, !Blueberry.getUtil().isOnGameThread());
    }

    protected TextureStitchEvent(@NotNull TextureAtlas textureAtlas, boolean async) {
        super(async);
        this.textureAtlas = textureAtlas;
    }

    @NotNull
    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    /**
     * Called when the texture is about to be stitched. You can use this event to register/add sprites.
     */
    public static class Pre extends TextureStitchEvent {
        private static final HandlerList handlerList = new HandlerList();
        private final Set<ResourceLocation> sprites;

        public Pre(@NotNull TextureAtlas textureAtlas, @NotNull Set<ResourceLocation> sprites) {
            super(textureAtlas);
            this.sprites = sprites;
        }

        public boolean addSprite(@NotNull ResourceLocation spriteLocation) {
            return this.sprites.add(spriteLocation);
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return handlerList;
        }
    }
}
