package net.blueberrymc.world.item;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;

public enum ItemRarity {
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.YELLOW),
    RARE(NamedTextColor.AQUA),
    EPIC(NamedTextColor.LIGHT_PURPLE),
    ;

    private final TextColor color;

    ItemRarity(TextColor color) {
        this.color = color;
    }

    @Contract(pure = true)
    public TextColor getColor() {
        return color;
    }
}
