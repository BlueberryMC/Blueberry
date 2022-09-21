package net.blueberrymc.world.item.crafting;

import net.kyori.adventure.key.Key;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface BlueberryRecipeManager {
    void addRecipe(@NotNull Recipe<?> recipe);
    void removeRecipe(@NotNull RecipeType<?> type, @NotNull Key id);
}
