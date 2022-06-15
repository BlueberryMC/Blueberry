package net.blueberrymc.client.gui;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Blueberry common gui components
 */
public class BlueberryGuiComponents {
    @ApiStatus.ScheduledForRemoval(inVersion = "1.5.0")
    @Deprecated(forRemoval = true)
    public static final TextComponent EMPTY_TEXT = new TextComponent("");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("blueberry", "textures/gui/icons.png");
}
