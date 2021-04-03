package net.blueberrymc.common.bml;

import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.registry.BlueberryRegistries;
import net.blueberrymc.world.item.SimpleBlueberryItem;
import net.blueberrymc.world.level.BlueberryLiquidBlock;
import net.blueberrymc.world.level.material.MilkFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InternalBlueberryMod extends BlueberryMod {
    private static final Timer clientTimer = new Timer("Async Client Blueberry Scheduler", true);
    private static final Timer serverTimer = new Timer("Async Server Blueberry Scheduler", true);
    private static final AtomicReference<String> lastScreen = new AtomicReference<>();
    public static MilkFluid FLOWING_MILK;
    public static MilkFluid MILK;
    public static Block MILK_BLOCK;
    public static boolean showDRPathfinding = false;
    public static boolean showDRWaterDebug = false;
    public static boolean showDRChunkBorder = true;
    public static boolean showDRHeightMap = false;
    public static boolean showDRCollisionBox = false;
    public static boolean showDRNeighborsUpdate = false;
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
    public static boolean liquidMilk = false;
    public static boolean item3d = false;
    public static boolean discordRpc = true;
    public static AtomicBoolean discordRpcShowServerIp = new AtomicBoolean(false);
    public static boolean bungee = false; // server only

    protected InternalBlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        super(modLoader, description, classLoader, file);
    }

    @Override
    public void onLoad() {
        getLogger().debug("ClassLoader: " + InternalBlueberryMod.class.getClassLoader().getClass().getCanonicalName());
        Blueberry.getUtil().updateDiscordStatus("Initializing the game", getStateList().getCurrentState().getName());
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
                        .add(new BooleanVisualConfig(new TextComponent("Pathfinding Renderer"), this.getConfig().getBoolean("debugRenderer.pathfinding", false)).id("debugRenderer.pathfinding"))
                        .add(new BooleanVisualConfig(new TextComponent("Water Debug Renderer"), this.getConfig().getBoolean("debugRenderer.waterDebug", false)).id("debugRenderer.waterDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Chunk Border Renderer"), this.getConfig().getBoolean("debugRenderer.chunkBorder", false)).id("debugRenderer.chunkBorder"))
                        .add(new BooleanVisualConfig(new TextComponent("Height Map Renderer"), this.getConfig().getBoolean("debugRenderer.heightMap", false)).id("debugRenderer.heightMap"))
                        .add(new BooleanVisualConfig(new TextComponent("Collision Box Renderer"), this.getConfig().getBoolean("debugRenderer.collisionBox", false)).id("debugRenderer.collisionBox"))
                        .add(new BooleanVisualConfig(new TextComponent("Neighbors Update Renderer"), this.getConfig().getBoolean("debugRenderer.neighborsUpdate", false)).id("debugRenderer.neighborsUpdate"))
                        .add(new BooleanVisualConfig(new TextComponent("Structure Renderer"), this.getConfig().getBoolean("debugRenderer.structure", false)).id("debugRenderer.structure"))
                        .add(new BooleanVisualConfig(new TextComponent("Light Debug Renderer"), this.getConfig().getBoolean("debugRenderer.lightDebug", false)).id("debugRenderer.lightDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("World Gen Attempt Renderer"), this.getConfig().getBoolean("debugRenderer.worldGenAttempt", false)).id("debugRenderer.worldGenAttempt"))
                        .add(new BooleanVisualConfig(new TextComponent("Solid Face Renderer"), this.getConfig().getBoolean("debugRenderer.solidFace", false)).id("debugRenderer.solidFace"))
                        .add(new BooleanVisualConfig(new TextComponent("Chunk Renderer"), this.getConfig().getBoolean("debugRenderer.chunk", false)).id("debugRenderer.chunk"))
                        .add(new BooleanVisualConfig(new TextComponent("Brain Debug Renderer"), this.getConfig().getBoolean("debugRenderer.brainDebug", false)).id("debugRenderer.brainDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Village Sections Debug Renderer"), this.getConfig().getBoolean("debugRenderer.villageSectionsDebug", false)).id("debugRenderer.villageSectionsDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Bee Debug Renderer"), this.getConfig().getBoolean("debugRenderer.beeDebug", false)).id("debugRenderer.beeDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Raid Debug Renderer"), this.getConfig().getBoolean("debugRenderer.raidDebug", false)).id("debugRenderer.raidDebug"))
                        .add(new BooleanVisualConfig(new TextComponent("Goal Selector Renderer"), this.getConfig().getBoolean("debugRenderer.goalSelector", false)).id("debugRenderer.goalSelector"))
                        .add(new BooleanVisualConfig(new TextComponent("Game Test Debug Renderer"), this.getConfig().getBoolean("debugRenderer.gameTestDebug", false)).id("debugRenderer.gameTestDebug"))
        );
        this.getVisualConfig().add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.gamePlay.title"))
                //.add(new BooleanVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.gamePlay.liquidMilk"), this.getConfig().getBoolean("gamePlay.liquidMilk")).id("gamePlay.liquidMilk"))
        );
        this.getVisualConfig().add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.misc.title"))
                        .add(new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.misc.discord_rpc.title"))
                                .add(new BooleanVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.misc.discord_rpc.enabled"), this.getConfig().getBoolean("misc.discordRpc.enabled")).id("misc.discordRpc.enabled"))
                                .add(new BooleanVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.misc.discord_rpc.show_server_ip"), this.getConfig().getBoolean("misc.discordRpc.showServerIp")).id("misc.discordRpc.showServerIp")))
        );
        this.getVisualConfig().add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "blueberry.mod.config.test.title"))
                        .add(new BooleanVisualConfig(new TextComponent("\"3D\" Item"), this.getConfig().getBoolean("test.3d")).id("test.3d").description(new TextComponent("Yes!")))
        );
        registerVisualConfigTest();
        reload();
        Blueberry.getEventManager().registerEvents(this, new InternalBlueberryModListener(this));
        registerArgumentTypes();
        Blueberry.getUtil().getClientSchedulerOptional().ifPresent(scheduler ->
                clientTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        scheduler.tickAsync();
                    }
                }, 1, 1)
        );
        AbstractBlueberryScheduler serverScheduler = Blueberry.getUtil().getServerScheduler();
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MinecraftServer server;
                if (Blueberry.getSide() == Side.CLIENT) {
                    server = Blueberry.getUtil().asClient().getIntegratedServer();
                } else {
                    server = Blueberry.getUtil().asServer().getServer();
                }
                if (server != null) {
                    serverScheduler.tickAsync();
                }
            }
        }, 50, 50);
    }

    @Override
    public void onPreInit() {
        Blueberry.getUtil().updateDiscordStatus("Initializing the game", getStateList().getCurrentState().getName());
        FLOWING_MILK = MilkFluid.Flowing.INSTANCE;
        MILK = MilkFluid.Source.INSTANCE;
        MILK_BLOCK = new BlueberryLiquidBlock(MILK, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops());
        registerFluids();
        registerBlocks();
        registerItems();
    }

    @Override
    public void onInit() {
        if (isFirst()) {
            Blueberry.getUtil().updateDiscordStatus("Initializing the game", getStateList().getCurrentState().getName());
        }
    }

    private void registerVisualConfigTest() {
        CompoundVisualConfig config = new CompoundVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.description"));
        config.add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.nest1"))
                        .add(new CompoundVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.nest2")))
        );
        config.add(
                new BooleanVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.boolean"))
        );
        getVisualConfig().add(config);
    }

    private void registerItems() {
        if (item3d) {
            BlueberryRegistries.ITEM.register("blueberry", "3d", new SimpleBlueberryItem(this, new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC), item -> new BlueberryText("blueberry", "item.blueberry.3d")));
        }
    }

    @Override
    public void onPostInit() {
        refreshDiscordStatus();
    }

    @Override
    public void onUnload() {
        clientTimer.cancel();
        serverTimer.cancel();
    }

    private void registerBlocks() {
    }

    private void registerFluids() {
    }

    private void registerArgumentTypes() {
        ArgumentTypes.register("blueberry:modid", ModIdArgument.class, new EmptyArgumentSerializer<>(ModIdArgument::modId));
    }

    private void reload() {
        showDRPathfinding = getConfig().getBoolean("debugRenderer.pathfinding", false);
        showDRWaterDebug = getConfig().getBoolean("debugRenderer.waterDebug", false);
        showDRChunkBorder = getConfig().getBoolean("debugRenderer.chunkBorder", true);
        showDRHeightMap = getConfig().getBoolean("debugRenderer.heightMap", false);
        showDRCollisionBox = getConfig().getBoolean("debugRenderer.collisionBox", false);
        showDRNeighborsUpdate = getConfig().getBoolean("debugRenderer.neighborsUpdate", false);
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
        liquidMilk = getConfig().getBoolean("gamePlay.liquidMilk", false);
        item3d = getConfig().getBoolean("test.3d", false);
        discordRpc = getConfig().getBoolean("misc.discordRpc.enabled", true);
        discordRpcShowServerIp.set(getConfig().getBoolean("misc.discordRpc.showServerIp", false));
        if (Blueberry.getSide() == Side.SERVER) bungee = getConfig().getBoolean("bungeecord", false); // server only
        if (Blueberry.getSide() == Side.CLIENT) {
            DiscordRPCTaskExecutor.init(discordRpc);
            ModState currentState = getStateList().getCurrentState();
            if (currentState == ModState.AVAILABLE || currentState == ModState.UNLOADED) {
                refreshDiscordStatus(Minecraft.getInstance().screen);
            }
        }
    }

    public void refreshDiscordStatus() {
        if (Blueberry.getSide() == Side.CLIENT) refreshDiscordStatus(Minecraft.getInstance().screen);
    }

    public void refreshDiscordStatus(@Nullable Screen screen) {
        if (Objects.equals(lastScreen.get(), screen == null ? null : screen.getClass().getCanonicalName())) return;
        Minecraft minecraft = Minecraft.getInstance();
        ServerData serverData = minecraft.getCurrentServer();
        if (screen instanceof JoinMultiplayerScreen) {
            Blueberry.getUtil().updateDiscordStatus("In Server List Menu", Blueberry.getModLoader().getActiveMods().size() + " mods active");
            lastScreen.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof TitleScreen) {
            Blueberry.getUtil().updateDiscordStatus("In Main Menu", Blueberry.getModLoader().getActiveMods().size() + " mods active");
            lastScreen.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof SelectWorldScreen) {
            Blueberry.getUtil().updateDiscordStatus("In Select World Menu");
            lastScreen.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof ConnectScreen && serverData != null) {
            Blueberry.getUtil().updateDiscordStatus("Connecting to server", discordRpcShowServerIp.get() ? serverData.ip : null);
            lastScreen.set(screen.getClass().getCanonicalName());
            return;
        }
        if (screen == null) {
            LocalPlayer player = minecraft.player;
            if (player == null) {
                Blueberry.getUtil().updateDiscordStatus("In Main Menu");
                lastScreen.set(null);
                return;
            }
            IntegratedServer integratedServer = minecraft.getSingleplayerServer();
            if (minecraft.isLocalServer() && integratedServer != null) {
                Blueberry.getUtil().updateDiscordStatus("Playing on Single Player", integratedServer.getWorldData().getLevelName(), BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                lastScreen.set(null);
                return;
            }
            if (minecraft.isConnectedToRealms()) {
                Blueberry.getUtil().updateDiscordStatus("Playing on Minecraft Realms", null, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                lastScreen.set(null);
                return;
            }
            if (serverData != null) {
                if (serverData.isLan()) {
                    Blueberry.getUtil().updateDiscordStatus("Playing on LAN server", null, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                    lastScreen.set(null);
                    return;
                } else {
                    Blueberry.getUtil().updateDiscordStatus("Playing on third-party server", discordRpcShowServerIp.get() ? serverData.ip : null, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                    lastScreen.set(null);
                    return;
                }
            }
        }
    }
}
