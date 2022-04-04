package net.blueberrymc.client.event;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class TextureStitchEvent extends Event {
    private final TextureAtlas textureAtlas;

    protected TextureStitchEvent(@NotNull TextureAtlas textureAtlas) {
        this(textureAtlas, !Blueberry.getUtil().isOnGameThread());
    }

    protected TextureStitchEvent(@NotNull TextureAtlas textureAtlas, boolean async) {
        super(async);
        this.textureAtlas = textureAtlas;
    }

    /**
     * Gets the texture atlas that is being stitched.
     * @return the texture
     */
    @NotNull
    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    /**
     * Called when the texture is about to be stitched. You can use this event to register/add sprites.
     */
    public static class Pre extends TextureStitchEvent {
        private final Set<ResourceLocation> sprites;

        public Pre(@NotNull TextureAtlas textureAtlas, @NotNull Set<ResourceLocation> sprites) {
            super(textureAtlas);
            this.sprites = sprites;
        }

        /**
         * Registers a sprite.
         * @param spriteLocation the location of the sprite
         * @return true if added; false otherwise
         */
        public boolean addSprite(@NotNull ResourceLocation spriteLocation) {
            return this.sprites.add(spriteLocation);
        }
    }
}
