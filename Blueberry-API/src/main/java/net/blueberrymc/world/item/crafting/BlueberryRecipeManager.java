package net.blueberrymc.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface BlueberryRecipeManager {
    void addRecipe(@NotNull RecipeHolder<?> recipe);
    void removeRecipe(@NotNull RecipeType<?> type, @NotNull ResourceLocation id);
}
