package net.blueberrymc.common;

import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.blueberrymc.common.item.SimpleBlueberryItem;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.registry.BlueberryRegistries;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class InternalBlueberryMod extends BlueberryMod {
    public static boolean showDRPathfinding = false;
    public static boolean showDRWaterDebug = false;
    public static boolean showDRChunkBorder = true;
    public static boolean showDRHeightMap = false;
    public static boolean showDRCollisionBox = false;
    public static boolean showDRNeighborsUpdate = false;
    public static boolean showDRCave = false;
    public static boolean showDRStructure = false;
    public static boolean showDRLightDebug = false;
    public static boolean showDRWorldGenAttempt = false;
    public static boolean showDRSolidFace = false;
    public static boolean showDRChunk = false;
    public static boolean showDRBrainDebug = false;
    public static boolean showDRVillageSectionsDebug = false;
    public static boolean showDRBeeDebug = false;
    public static boolean showDRRaidDebug = false;
    public static boolean showDRGoalSelector = false;
    public static boolean showDRGameTestDebug = false;
    public static boolean item3d = false;

    @SuppressWarnings("deprecation")
    public static void register() {
        ModDescriptionFile description = new ModDescriptionFile(
                "blueberry",
                Versioning.getVersion().getFullyQualifiedVersion(),
                "net.blueberrymc.common.InternalBlueberryMod",
                "Blueberry",
                "Blueberry development team",
                "MagmaCube",
                Arrays.asList("Modding API for Minecraft", "Disabling this mod has no effect on functionally."),
                true,
                Collections.singletonList("magmaCube"));
        Blueberry.getModLoader().forceRegisterMod(description, InternalBlueberryMod.class);
    }

    @Override
    public void onLoad() {
        this.getVisualConfig().onSave = config -> {
            this.save(config);
            reload();
            try {
                getConfig().saveConfig();
                this.getLogger().info("Saved configuration");
            } catch (IOException ex) {
                this.getLogger().error("Could not save configuration", ex);
            }
        };
        this.getVisualConfig().add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.debugRenderer.title"))
                        .add(new BooleanVisualConfig(new TextComponent("Pathfinding Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.pathfinding", false)).id("debugRenderer.pathfinding"))
                        .add(new BooleanVisualConfig(new TextComponent("Water Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.waterDebug", false)).id("debugRenderer.waterDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Chunk Border Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.chunkBorder", false)).id("debugRenderer.chunkBorder"))
                        .add(new BooleanVisualConfig(new TextComponent("Height Map Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.heightMap", false)).id("debugRenderer.heightMap"))
                        .add(new BooleanVisualConfig(new TextComponent("Collision Box Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.collisionBox", false)).id("debugRenderer.collisionBox"))
                        .add(new BooleanVisualConfig(new TextComponent("Neighbors Update Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.neighborsUpdate", false)).id("debugRenderer.neighborsUpdate"))
                        .add(new BooleanVisualConfig(new TextComponent("Cave Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.cave", false)).id("debugRenderer.cave"))
                        .add(new BooleanVisualConfig(new TextComponent("Structure Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.structure", false)).id("debugRenderer.structure"))
                        .add(new BooleanVisualConfig(new TextComponent("Light Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.lightDebug", false)).id("debugRenderer.lightDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("World Gen Attempt Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.worldGenAttempt", false)).id("debugRenderer.worldGenAttempt"))
                        .add(new BooleanVisualConfig(new TextComponent("Solid Face Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.solidFace", false)).id("debugRenderer.solidFace"))
                        .add(new BooleanVisualConfig(new TextComponent("Chunk Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.chunk", false)).id("debugRenderer.chunk"))
                        .add(new BooleanVisualConfig(new TextComponent("Brain Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.brainDebug", false)).id("debugRenderer.brainDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Village Sections Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.villageSectionsDebug", false)).id("debugRenderer.villageSectionsDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Bee Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.beeDebug", false)).id("debugRenderer.beeDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Raid Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.raidDebug", false)).id("debugRenderer.raidDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Goal Selector Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.goalSelector", false)).id("debugRenderer.goalSelector"))
                        .add(new BooleanVisualConfig(new TextComponent("Game Test Debug Renderer"), this.getConfig().getConfig().getBoolean("debugRenderer.gameTestDebug", false)).id("debugRenderer.gameTestDebug"))
        ).add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.test.title"))
                        .add(new BooleanVisualConfig(new TextComponent("\"3D\" Item"), this.getConfig().getConfig().getBoolean("test.3d")).id("test.3d"))
        );
        reload();
    }

    public void save(CompoundVisualConfig compoundVisualConfig) {
        for (VisualConfig<?> config : compoundVisualConfig) {
            if (config instanceof CompoundVisualConfig) {
                save((CompoundVisualConfig) config);
                continue;
            }
            InternalBlueberryMod.this.getConfig().set(config.getId(), config.get());
        }
    }

    @Override
    public void onPreInit() {
        this.getLogger().info("fuck you");
        BlueberryRegistries.ITEM.register("blueberry", "3d", new SimpleBlueberryItem(this, new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC), item -> new BlueberryText("blueberry", "item.blueberry.3d")));
    }

    public void reload() {
        showDRPathfinding = getConfig().getBoolean("debugRenderer.pathfinding", false);
        showDRWaterDebug = getConfig().getBoolean("debugRenderer.waterDebug", false);
        showDRChunkBorder = getConfig().getBoolean("debugRenderer.chunkBorder", true);
        showDRHeightMap = getConfig().getBoolean("debugRenderer.heightMap", false);
        showDRCollisionBox = getConfig().getBoolean("debugRenderer.collisionBox", false);
        showDRNeighborsUpdate = getConfig().getBoolean("debugRenderer.neighborsUpdate", false);
        showDRCave = getConfig().getBoolean("debugRenderer.cave", false);
        showDRStructure = getConfig().getBoolean("debugRenderer.structure", false);
        showDRLightDebug = getConfig().getBoolean("debugRenderer.lightDebug", false);
        showDRWorldGenAttempt = getConfig().getBoolean("debugRenderer.worldGenAttempt", false);
        showDRSolidFace = getConfig().getBoolean("debugRenderer.solidFace", false);
        showDRChunk = getConfig().getBoolean("debugRenderer.chunk", false);
        showDRBrainDebug = getConfig().getBoolean("debugRenderer.brainDebug", false);
        showDRVillageSectionsDebug = getConfig().getBoolean("debugRenderer.villageSectionsDebug", false);
        showDRBeeDebug = getConfig().getBoolean("debugRenderer.beeDebug", false);
        showDRRaidDebug = getConfig().getBoolean("debugRenderer.raidDebug", false);
        showDRGoalSelector = getConfig().getBoolean("debugRenderer.goalSelector", false);
        showDRGameTestDebug = getConfig().getBoolean("debugRenderer.gameTestDebug", false);
        item3d = getConfig().getBoolean("test.3d", false);
    }
}
