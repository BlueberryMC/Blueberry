package net.blueberrymc.world.item.crafting;

import com.google.common.base.Preconditions;
import net.blueberrymc.util.WeakList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RecipeBuilder {
    private static final Map<Identifier, RecipeHolder<?>> RECIPES = new HashMap<>();
    public static final WeakList<RecipeManager> knownRecipeManagers = new WeakList<>();
    @NotNull
    public static final Ingredient AIR = Ingredient.of(Items.AIR);

    @Contract("_, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull Identifier id, @NotNull ItemLike result) {
        return new Shaped(id, result.asItem());
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull Identifier id, @NotNull ItemLike result, int count) {
        return new Shaped(id, result.asItem(), count);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shaped shaped(@NotNull Identifier id, @NotNull ItemStackTemplate itemStack) {
        return new Shaped(id, itemStack);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull Identifier id, @NotNull ItemLike result) {
        return new Shapeless(id, result.asItem());
    }

    @Contract("_, _, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull Identifier id, @NotNull ItemLike result, int count) {
        return new Shapeless(id, result.asItem(), count);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static Shapeless shapeless(@NotNull Identifier id, @NotNull ItemStackTemplate itemStack) {
        return new Shapeless(id, itemStack);
    }

    @NotNull
    public static Map<Identifier, RecipeHolder<?>> getRecipes() {
        return RECIPES;
    }

    @Nullable
    public static RecipeHolder<?> removeFromRecipeManager(@NotNull RecipeHolder<?> recipe) {
        return removeFromRecipeManager(recipe.value().getType(), recipe.id().identifier());
    }

    @Nullable
    public static RecipeHolder<?> removeFromRecipeManager(@NotNull RecipeType<?> type, @NotNull Identifier id) {
        knownRecipeManagers.bake().forEach(rm -> ((BlueberryRecipeManager) rm).removeRecipe(type, id));
        return getRecipes().remove(id);
    }

    @NotNull
    public abstract RecipeHolder<?> build();

    public final void addToRecipeManager() {
        RecipeHolder<?> recipe = build();
        if (getRecipes().containsKey(recipe.id().identifier())) throw new IllegalArgumentException("Duplicate recipe: " + recipe.id() + ", " + recipe);
        getRecipes().put(recipe.id().identifier(), recipe);
        knownRecipeManagers.bake().forEach(rm -> ((BlueberryRecipeManager) rm).addRecipe(recipe));
    }

    public final void removeFromRecipeManager() {
        RecipeHolder<?> recipe = build();
        removeFromRecipeManager(recipe.value().getType(), recipe.id().identifier());
    }

    public static class Shaped extends RecipeBuilder {
        private final Identifier id;
        private final ItemStackTemplate result;
        private Recipe.CommonInfo commonInfo = new Recipe.CommonInfo(true);
        private CraftingRecipe.CraftingBookInfo category = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "Blueberry");
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Ingredient> key = new HashMap<>();

        public Shaped(@NotNull Identifier id, @NotNull Item result) {
            this(id, result, 1);
        }

        public Shaped(@NotNull Identifier id, @NotNull Item result, int count) {
            this(id, new ItemStackTemplate(result, Math.max(count, 1)));
        }

        public Shaped(@NotNull Identifier id, @NotNull ItemStackTemplate result) {
            Preconditions.checkNotNull(id, "id cannot be null");
            Preconditions.checkNotNull(result, "result cannot be null");
            this.id = id;
            this.result = result;
        }

        @NotNull
        public Shaped commonInfo(@NotNull Recipe.CommonInfo commonInfo) {
            Preconditions.checkNotNull(commonInfo, "commonInfo cannot be null");
            this.commonInfo = commonInfo;
            return this;
        }

        @NotNull
        public Shaped category(@NotNull CraftingRecipe.CraftingBookInfo category) {
            Preconditions.checkNotNull(category, "category cannot be null");
            this.category = category;
            return this;
        }

        @NotNull
        public Shaped pattern(@NotNull String pattern) {
            if (!this.rows.isEmpty() && pattern.length() != this.rows.getFirst().length())
                throw new IllegalArgumentException("Pattern must be the same width on every line!");
            this.rows.add(pattern);
            return this;
        }

        @NotNull
        public Shaped define(char character, @NotNull HolderSet<Item> tag) {
            return this.define(character, Ingredient.of(tag));
        }

        @NotNull
        public Shaped define(char character, @NotNull ItemLike itemLike) {
            return this.define(character, Ingredient.of(itemLike));
        }

        @NotNull
        public Shaped define(char character, @NotNull ItemStack itemStack) {
            return this.define(character, Ingredient.of(itemStack.getItem()));
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
        public RecipeHolder<ShapedRecipe> build() {
            if (rows.isEmpty()) throw new IllegalArgumentException("row list is empty");
            if (key.isEmpty()) throw new IllegalArgumentException("key list is empty");
            List<Optional<Ingredient>> list = new ArrayList<>(rows.getFirst().length() * rows.size());
            AtomicInteger index = new AtomicInteger();
            rows.forEach(s -> {
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c != ' ') {
                        if (!key.containsKey(c)) throw new IllegalArgumentException("Symbol '" + c + "' is not defined!");
                        list.set(index.get(), Optional.ofNullable(key.get(c)));
                    }
                    index.getAndIncrement();
                }
            });
            ShapedRecipePattern pattern = new ShapedRecipePattern(getWidth(), getHeight(), list, Optional.of(new ShapedRecipePattern.Data(key, rows)));
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), new ShapedRecipe(commonInfo, category, pattern, result));
        }

        // --- getters

        @NotNull
        public Identifier getId() {
            return id;
        }

        public int getWidth() {
            return rows.isEmpty() ? 0 : rows.getFirst().length();
        }

        public int getHeight() {
            return rows.size();
        }

        @NotNull
        public ItemStackTemplate getResult() {
            return result;
        }
    }

    public static class Shapeless extends RecipeBuilder {
        private final Identifier id;
        private final ItemStackTemplate result;
        private Recipe.CommonInfo commonInfo = new Recipe.CommonInfo(true);
        private final NonNullList<Ingredient> ingredients = NonNullList.create();
        private CraftingRecipe.CraftingBookInfo category = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "Blueberry");

        public Shapeless(@NotNull Identifier id, @NotNull Item item) {
            this(id, item, 1);
        }

        public Shapeless(@NotNull Identifier id, @NotNull Item item, int count) {
            this(id, new ItemStackTemplate(item, Math.max(count, 1)));
        }

        public Shapeless(@NotNull Identifier id, @NotNull ItemStackTemplate result) {
            this.id = id;
            this.result = result;
        }

        @NotNull
        public Shapeless commonInfo(@NotNull Recipe.CommonInfo commonInfo) {
            Preconditions.checkNotNull(commonInfo, "commonInfo cannot be null");
            this.commonInfo = commonInfo;
            return this;
        }

        @NotNull
        public Shapeless category(@NotNull CraftingRecipe.CraftingBookInfo category) {
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
            ingredients.add(Ingredient.of(itemStack.getItem()));
            return this;
        }

        @NotNull
        public Shapeless requires(@NotNull HolderSet<Item> tag) {
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
        public RecipeHolder<ShapelessRecipe> build() {
            if (ingredients.isEmpty()) throw new IllegalArgumentException("ingredient list is empty");
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), new ShapelessRecipe(commonInfo, category, result, ingredients));
        }

        // --- getters

        @NotNull
        public Identifier getId() {
            return id;
        }

        @NotNull
        public ItemStackTemplate getResult() {
            return result;
        }
    }
}
