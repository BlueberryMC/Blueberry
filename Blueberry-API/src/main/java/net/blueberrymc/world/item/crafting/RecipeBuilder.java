package net.blueberrymc.world.item.crafting;

import com.google.common.base.Preconditions;
import net.blueberrymc.util.WeakList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RecipeBuilder {
    private static final Map<ResourceLocation, Recipe<?>> RECIPES = new HashMap<>();
    public static final WeakList<RecipeManager> knownRecipeManagers = new WeakList<>();
    @NotNull
    public static final Ingredient AIR = Ingredient.of(Items.AIR);

    @Contract("_, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull ResourceLocation id, @NotNull ItemLike result) {
        return new Shaped(id, result.asItem());
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull ResourceLocation id, @NotNull ItemLike result, int count) {
        return new Shaped(id, result.asItem(), count);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull ResourceLocation id, @NotNull ItemStack itemStack) {
        return new Shaped(id, itemStack);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull ResourceLocation id, @NotNull ItemLike result) {
        return new Shapeless(id, result.asItem());
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull ResourceLocation id, @NotNull ItemLike result, int count) {
        return new Shapeless(id, result.asItem(), count);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull ResourceLocation id, @NotNull ItemStack itemStack) {
        return new Shapeless(id, itemStack);
    }

    @NotNull
    public static Map<ResourceLocation, Recipe<?>> getRecipes() {
        return RECIPES;
    }

    @Nullable
    public static Recipe<?> removeFromRecipeManager(@NotNull Recipe<?> recipe) {
        return removeFromRecipeManager(recipe.getType(), recipe.getId());
    }

    @Nullable
    public static Recipe<?> removeFromRecipeManager(@NotNull RecipeType<?> type, @NotNull ResourceLocation id) {
        knownRecipeManagers.bake().forEach(rm -> ((BlueberryRecipeManager) rm).removeRecipe(type, id));
        return getRecipes().remove(id);
    }

    @NotNull
    public abstract Recipe<?> build();

    public final void addToRecipeManager() {
        Recipe<?> recipe = build();
        if (getRecipes().containsKey(recipe.getId())) throw new IllegalArgumentException("Duplicate recipe: " + recipe.getId() + ", " + recipe);
        getRecipes().put(recipe.getId(), recipe);
        knownRecipeManagers.bake().forEach(rm -> ((BlueberryRecipeManager) rm).addRecipe(recipe));
    }

    public final void removeFromRecipeManager() {
        Recipe<?> recipe = build();
        removeFromRecipeManager(recipe.getType(), recipe.getId());
    }

    public static class Shaped extends RecipeBuilder {
        private final ResourceLocation id;
        private final ItemStack result;
        private String group = "";
        private CraftingBookCategory category = CraftingBookCategory.MISC;
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Ingredient> key = new HashMap<>();

        public Shaped(@NotNull ResourceLocation id, @NotNull Item result) {
            this(id, result, 1);
        }

        public Shaped(@NotNull ResourceLocation id, @NotNull Item result, int count) {
            this(id, new ItemStack(result, Math.max(count, 1)));
        }

        public Shaped(@NotNull ResourceLocation id, @NotNull ItemStack result) {
            Preconditions.checkNotNull(id, "id cannot be null");
            Preconditions.checkNotNull(result, "result cannot be null");
            this.id = id;
            this.result = result;
        }

        @NotNull
        public Shaped group(@NotNull String group) {
            Preconditions.checkNotNull(group, "group cannot be null");
            this.group = group;
            return this;
        }

        @NotNull
        public Shaped category(@NotNull CraftingBookCategory category) {
            Preconditions.checkNotNull(category, "category cannot be null");
            this.category = category;
            return this;
        }

        @NotNull
        public Shaped pattern(@NotNull String pattern) {
            if (!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length())
                throw new IllegalArgumentException("Pattern must be the same width on every line!");
            this.rows.add(pattern);
            return this;
        }

        @NotNull
        public Shaped define(char character, @NotNull TagKey<Item> tag) {
            return this.define(character, Ingredient.of(tag));
        }

        @NotNull
        public Shaped define(char character, @NotNull ItemLike itemLike) {
            return this.define(character, Ingredient.of(itemLike));
        }

        @NotNull
        public Shaped define(char character, @NotNull ItemStack itemStack) {
            return this.define(character, Ingredient.of(itemStack));
        }

        @NotNull
        public Shaped define(char character, @NotNull Ingredient ingredient) {
            if (this.key.containsKey(character)) {
                throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
            } else if (character == ' ') {
                throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
            } else {
                this.key.put(character, ingredient);
                return this;
            }
        }

        @NotNull
        @Override
        public ShapedRecipe build() {
            if (rows.isEmpty()) throw new IllegalArgumentException("row list is empty");
            if (key.isEmpty()) throw new IllegalArgumentException("key list is empty");
            NonNullList<Ingredient> list = NonNullList.withSize(rows.get(0).length() * rows.size(), AIR);
            AtomicInteger index = new AtomicInteger();
            rows.forEach(s -> {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c != ' ') {
                        if (!key.containsKey(c)) throw new IllegalArgumentException("Symbol '" + c + "' is not defined!");
                        list.set(index.get(), key.get(c));
                    }
                    index.getAndIncrement();
                }
            });
            return new ShapedRecipe(id, group, category, getWidth(), getHeight(), list, result);
        }

        // --- getters

        @NotNull
        public ResourceLocation getId() {
            return id;
        }

        public int getWidth() {
            return rows.isEmpty() ? 0 : rows.get(0).length();
        }

        public int getHeight() {
            return rows.size();
        }

        @NotNull
        public ItemStack getResult() {
            return result;
        }
    }

    public static class Shapeless extends RecipeBuilder {
        private final ResourceLocation id;
        private final ItemStack result;
        private String group = "";
        private final NonNullList<Ingredient> ingredients = NonNullList.create();
        private CraftingBookCategory category = CraftingBookCategory.MISC;

        public Shapeless(@NotNull ResourceLocation id, @NotNull Item item) {
            this(id, item, 1);
        }

        public Shapeless(@NotNull ResourceLocation id, @NotNull Item item, int count) {
            this(id, new ItemStack(item, Math.max(count, 1)));
        }

        public Shapeless(@NotNull ResourceLocation id, @NotNull ItemStack result) {
            this.id = id;
            this.result = result;
        }

        @NotNull
        public Shapeless group(@NotNull String group) {
            Preconditions.checkNotNull(group, "group cannot be null");
            this.group = group;
            return this;
        }

        @NotNull
        public Shapeless category(@NotNull CraftingBookCategory category) {
            Preconditions.checkNotNull(category, "category cannot be null");
            this.category = category;
            return this;
        }

        @NotNull
        public Shapeless requires(@NotNull ItemLike itemLike) {
            ingredients.add(Ingredient.of(itemLike));
            return this;
        }

        @NotNull
        public Shapeless requires(@NotNull ItemStack itemStack) {
            ingredients.add(Ingredient.of(itemStack));
            return this;
        }

        @NotNull
        public Shapeless requires(@NotNull TagKey<Item> tag) {
            ingredients.add(Ingredient.of(tag));
            return this;
        }

        @NotNull
        public Shapeless requires(@NotNull Ingredient ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        @NotNull
        @Override
        public ShapelessRecipe build() {
            if (ingredients.isEmpty()) throw new IllegalArgumentException("ingredient list is empty");
            return new ShapelessRecipe(id, group, category, result, ingredients);
        }

        // --- getters

        @NotNull
        public ResourceLocation getId() {
            return id;
        }

        @NotNull
        public ItemStack getResult() {
            return result;
        }
    }
}
