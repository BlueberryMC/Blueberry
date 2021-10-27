package net.blueberrymc.registry;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.blueberrymc.common.bml.ModClassLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class BlueberryRegistries<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    static { init(); }

    public static final BlueberryRegistries<ParticleType<?>> PARTICLE_TYPES = new BlueberryRegistries<>(Registry.PARTICLE_TYPE);
    public static final BlueberryRegistries<Block> BLOCK = new BlueberryRegistries<>(Registry.BLOCK, block -> {
        for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
            Block.BLOCK_STATE_REGISTRY.add(blockState);
        }
        block.getLootTable(); // initialize loot table key
    });
    public static final BlueberryRegistries<Item> ITEM = new BlueberryRegistries<>(Registry.ITEM);
    public static final BlueberryRegistries<MenuType<?>> MENU = new BlueberryRegistries<>(Registry.MENU);
    public static final BlueberryRegistries<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new BlueberryRegistries<>(Registry.BLOCK_ENTITY_TYPE);
    public static final BlueberryRegistries<Fluid> FLUID = new BlueberryRegistries<>(Registry.FLUID, fluid -> {
        for (FluidState fluidState : fluid.getStateDefinition().getPossibleStates()) {
            Fluid.FLUID_STATE_REGISTRY.add(fluidState);
        }
    });
    public static final BlueberryRegistries<SoundEvent> SOUND_EVENT = new BlueberryRegistries<>(Registry.SOUND_EVENT);
    public static final BlueberryRegistries<MobEffect> MOB_EFFECT = new BlueberryRegistries<>(Registry.MOB_EFFECT);
    public static final BlueberryRegistries<Enchantment> ENCHANTMENT = new BlueberryRegistries<>(Registry.ENCHANTMENT);
    public static final BlueberryRegistries<EntityType<?>> ENTITY_TYPE = new BlueberryRegistries<>(Registry.ENTITY_TYPE);
    public static final BlueberryRegistries<Potion> POTION = new BlueberryRegistries<>(Registry.POTION);
    public static final BlueberryRegistries<ParticleType<?>> PARTICLE_TYPE = new BlueberryRegistries<>(Registry.PARTICLE_TYPE);
    public static final BlueberryRegistries<Motive> MOTIVE = new BlueberryRegistries<>(Registry.MOTIVE);
    public static final BlueberryRegistries<ResourceLocation> CUSTOM_STAT = new BlueberryRegistries<>(Registry.CUSTOM_STAT);
    public static final BlueberryRegistries<ChunkStatus> CHUNK_STATUS = new BlueberryRegistries<>(Registry.CHUNK_STATUS);
    public static final BlueberryRegistries<RuleTestType<?>> RULE_TEST = new BlueberryRegistries<>(Registry.RULE_TEST);
    public static final BlueberryRegistries<PosRuleTestType<?>> POS_RULE_TEST = new BlueberryRegistries<>(Registry.POS_RULE_TEST);
    public static final BlueberryRegistries<RecipeType<?>> RECIPE_TYPE = new BlueberryRegistries<>(Registry.RECIPE_TYPE);
    public static final BlueberryRegistries<RecipeSerializer<?>> RECIPE_SERIALIZER = new BlueberryRegistries<>(Registry.RECIPE_SERIALIZER);
    public static final BlueberryRegistries<Attribute> ATTRIBUTE = new BlueberryRegistries<>(Registry.ATTRIBUTE);
    public static final BlueberryRegistries<PositionSourceType<?>> POSITION_SOURCE_TYPE = new BlueberryRegistries<>(Registry.POSITION_SOURCE_TYPE);
    public static final BlueberryRegistries<StatType<?>> STAT_TYPE = new BlueberryRegistries<>(Registry.STAT_TYPE);
    public static final BlueberryRegistries<VillagerType> VILLAGER_TYPE = new BlueberryRegistries<>(Registry.VILLAGER_TYPE);
    public static final BlueberryRegistries<VillagerProfession> VILLAGER_PROFESSION = new BlueberryRegistries<>(Registry.VILLAGER_PROFESSION);
    public static final BlueberryRegistries<PoiType> POINT_OF_INTEREST_TYPE = new BlueberryRegistries<>(Registry.POINT_OF_INTEREST_TYPE);
    public static final BlueberryRegistries<MemoryModuleType<?>> MEMORY_MODULE_TYPE = new BlueberryRegistries<>(Registry.MEMORY_MODULE_TYPE);
    public static final BlueberryRegistries<SensorType<?>> SENSOR_TYPE = new BlueberryRegistries<>(Registry.SENSOR_TYPE);
    public static final BlueberryRegistries<Schedule> SCHEDULE = new BlueberryRegistries<>(Registry.SCHEDULE);
    public static final BlueberryRegistries<Activity> ACTIVITY = new BlueberryRegistries<>(Registry.ACTIVITY);
    public static final BlueberryRegistries<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = new BlueberryRegistries<>(Registry.LOOT_POOL_ENTRY_TYPE);
    public static final BlueberryRegistries<LootItemFunctionType> LOOT_FUNCTION_TYPE = new BlueberryRegistries<>(Registry.LOOT_FUNCTION_TYPE);
    public static final BlueberryRegistries<LootItemConditionType> LOOT_CONDITION_TYPE = new BlueberryRegistries<>(Registry.LOOT_CONDITION_TYPE);
    public static final BlueberryRegistries<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = new BlueberryRegistries<>(Registry.LOOT_NUMBER_PROVIDER_TYPE);
    public static final BlueberryRegistries<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = new BlueberryRegistries<>(Registry.LOOT_NBT_PROVIDER_TYPE);
    public static final BlueberryRegistries<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = new BlueberryRegistries<>(Registry.LOOT_SCORE_PROVIDER_TYPE);
    public static final BlueberryRegistries<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = new BlueberryRegistries<>(Registry.BLOCKSTATE_PROVIDER_TYPES);
    public static final BlueberryRegistries<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = new BlueberryRegistries<>(Registry.FOLIAGE_PLACER_TYPES);
    public static final BlueberryRegistries<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = new BlueberryRegistries<>(Registry.TRUNK_PLACER_TYPES);
    public static final BlueberryRegistries<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = new BlueberryRegistries<>(Registry.TREE_DECORATOR_TYPES);
    public static final BlueberryRegistries<FeatureSizeType<?>> FEATURE_SIZE_TYPES = new BlueberryRegistries<>(Registry.FEATURE_SIZE_TYPES);
    public static final BlueberryRegistries<Codec<? extends BiomeSource>> BIOME_SOURCE = new BlueberryRegistries<>(Registry.BIOME_SOURCE);
    public static final BlueberryRegistries<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR = new BlueberryRegistries<>(Registry.CHUNK_GENERATOR);
    public static final BlueberryRegistries<StructureProcessorType<?>> STRUCTURE_PROCESSOR = new BlueberryRegistries<>(Registry.STRUCTURE_PROCESSOR);
    public static final BlueberryRegistries<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = new BlueberryRegistries<>(Registry.STRUCTURE_POOL_ELEMENT);
    public static final BlueberryRegistries<WorldCarver<?>> CARVER = new BlueberryRegistries<>(Registry.CARVER);
    public static final BlueberryRegistries<Feature<?>> FEATURE = new BlueberryRegistries<>(Registry.FEATURE);
    public static final BlueberryRegistries<StructureFeature<?>> STRUCTURE_FEATURE = new BlueberryRegistries<>(Registry.STRUCTURE_FEATURE);
    public static final BlueberryRegistries<StructurePieceType> STRUCTURE_PIECE = new BlueberryRegistries<>(Registry.STRUCTURE_PIECE);
    public static final BlueberryRegistries<FeatureDecorator<?>> DECORATOR = new BlueberryRegistries<>(Registry.DECORATOR);
    public static final BlueberryRegistries<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = new BlueberryRegistries<>(BuiltinRegistries.CONFIGURED_FEATURE);
    public static final BlueberryRegistries<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = new BlueberryRegistries<>(BuiltinRegistries.CONFIGURED_CARVER);
    public static final BlueberryRegistries<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = new BlueberryRegistries<>(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE);
    public static final BlueberryRegistries<StructureProcessorList> PROCESSOR_LIST = new BlueberryRegistries<>(BuiltinRegistries.PROCESSOR_LIST);
    public static final BlueberryRegistries<StructureTemplatePool> TEMPLATE_POOL = new BlueberryRegistries<>(BuiltinRegistries.TEMPLATE_POOL);
    public static final BlueberryRegistries<Biome> BIOME = new BlueberryRegistries<>(BuiltinRegistries.BIOME);
    public static final BlueberryRegistries<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = new BlueberryRegistries<>(BuiltinRegistries.NOISE_GENERATOR_SETTINGS);

    @NotNull private final Registry<T> registry;
    @Nullable private final Consumer<T> registerAction;

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static <T> BlueberryRegistries<T> of(@NotNull Registry<T> registry) {
        return of(registry, null);
    }

    @Contract(value = "_, _ -> new", pure = true)
    @NotNull
    public static <T> BlueberryRegistries<T> of(@NotNull Registry<T> registry, @Nullable Consumer<T> registerAction) {
        return new BlueberryRegistries<>(registry, registerAction);
    }

    private BlueberryRegistries(@NotNull Registry<T> registry) {
        this(registry, null);
    }

    private BlueberryRegistries(@NotNull Registry<T> registry, @Nullable Consumer<T> registerAction) {
        this.registry = registry;
        this.registerAction = registerAction;
    }

    @NotNull
    public Registry<T> getRegistry() { return registry; }

    public void register(@NotNull String id, @NotNull T object) {
        if (object.getClass().getClassLoader() instanceof ModClassLoader) {
            register(((ModClassLoader) object.getClass().getClassLoader()).getMod().getDescription().getModId().toLowerCase(Locale.ROOT), id, object);
        }
    }

    @NotNull
    public <R extends T> R register(@NotNull String namespace, @NotNull String id, @NotNull R object) {
        //if (has(namespace, id)) return; // TODO: figure out why it returns true even if it's not yet registered
        return register(new ResourceLocation(namespace, id), object);
    }

    @Nullable
    public T get(@NotNull ResourceLocation location) {
        return registry.get(location);
    }

    @Nullable
    public T get(@NotNull String namespace, @NotNull String id) {
        return get(new ResourceLocation(namespace, id));
    }

    public int getId(@NotNull T t) {
        return registry.getId(t);
    }

    @Nullable
    public T byId(int id) {
        return registry.byId(id);
    }

    public boolean has(@NotNull ResourceLocation location) {
        return get(location) != null;
    }

    public boolean has(@NotNull String namespace, @NotNull String id) {
        return get(namespace, id) != null;
    }

    @NotNull
    public <R extends T> R register(@NotNull ResourceLocation location, @NotNull R object) {
        Preconditions.checkNotNull(location, "ResourceLocation cannot be null");
        Preconditions.checkNotNull(object, "value cannot be null");
        LOGGER.info("Registering " + object.getClass().getCanonicalName() + ": " + location);
        if (object instanceof BlockItem) {
            ((BlockItem) object).registerBlocks(Item.BY_BLOCK, (Item) object);
        }
        R result = Registry.register(registry, location, object);
        if (registerAction != null) registerAction.accept(object);
        return result;
    }

    public static synchronized <T extends BlockEntity> void bindTileEntityRenderer(
            @NotNull BlockEntityType<T> blockEntityType,
            @NotNull Function<? super BlockEntityRenderDispatcher, ? extends BlockEntityRenderer<? super T>> rendererFactory
    ) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher()).registerSpecialRenderer(blockEntityType, rendererFactory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher()));
    }

    private static void init() {
        Registry.BLOCK.byId(1);
    }
}
