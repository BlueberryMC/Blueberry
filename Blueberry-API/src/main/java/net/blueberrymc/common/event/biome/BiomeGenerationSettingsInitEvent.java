package net.blueberrymc.common.event.biome;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the biome generation settings is being created from the builder.
 */
public class BiomeGenerationSettingsInitEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final BiomeGenerationSettings.Builder builder;
    private BiomeGenerationSettings biomeGenerationSettings;

    public BiomeGenerationSettingsInitEvent(@NotNull BiomeGenerationSettings.Builder builder, @NotNull BiomeGenerationSettings biomeGenerationSettings) {
        this.builder = builder;
        this.biomeGenerationSettings = biomeGenerationSettings;
    }

    /**
     * Gets the builder for the event.
     * @return the builder
     */
    @NotNull
    public BiomeGenerationSettings.Builder getBuilder() {
        return builder;
    }

    /**
     * Gets the biome generation settings being created.
     * @return the biome generation settings
     */
    @NotNull
    public BiomeGenerationSettings getBiomeGenerationSettings() {
        return biomeGenerationSettings;
    }

    public void setBiomeGenerationSettings(@NotNull BiomeGenerationSettings biomeGenerationSettings) {
        Preconditions.checkNotNull(biomeGenerationSettings, "biomeGenerationSettings cannot be null");
        this.biomeGenerationSettings = biomeGenerationSettings;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
