package net.blueberrymc.common.event.biome;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.event.Event;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Called when the biome generation settings is being created from the builder.
 */
public class BiomeGenerationSettingsInitEvent extends Event {
    private final BiomeGenerationSettings.Builder builder;
    private Supplier<BiomeGenerationSettings> biomeGenerationSettings;

    public BiomeGenerationSettingsInitEvent(@NotNull BiomeGenerationSettings.Builder builder, @NotNull Supplier<BiomeGenerationSettings> biomeGenerationSettings) {
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
    public Supplier<BiomeGenerationSettings> getBiomeGenerationSettings() {
        return biomeGenerationSettings;
    }

    public void setBiomeGenerationSettings(@NotNull Supplier<BiomeGenerationSettings> biomeGenerationSettings) {
        Preconditions.checkNotNull(biomeGenerationSettings, "biomeGenerationSettings cannot be null");
        this.biomeGenerationSettings = biomeGenerationSettings;
    }
}
