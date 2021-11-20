package net.blueberrymc.common.bml;

import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.command.argument.ModIdArgument;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfigManager;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.registry.BlueberryRegistries;
import net.blueberrymc.util.NameGetter;
import net.blueberrymc.world.item.SimpleBlueberryItem;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class InternalBlueberryMod extends BlueberryMod {
    private static final Timer clientTimer = new Timer("Async Client Blueberry Scheduler", true);
    private static final Timer serverTimer = new Timer("Async Server Blueberry Scheduler", true);
    private static final AtomicReference<String> lastScreen = new AtomicReference<>();
    private static DiscordRPCStatus lastDiscordRPCStatus = null;

    protected InternalBlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        super(modLoader, description, classLoader, file);
    }

    @Override
    public void onLoad() {
        getLogger().debug("ClassLoader: " + InternalBlueberryMod.class.getClassLoader().getClass().getCanonicalName());
        Blueberry.getUtil().updateDiscordStatus("Initializing the game", getStateList().getCurrentState().getName());
        onReload();
        this.setVisualConfig(VisualConfigManager.createFromClass(InternalBlueberryModConfig.class));
        Blueberry.runOnClient(() -> {
            this.getVisualConfig().onSave(this::saveConfig);
            Blueberry.getUtil().getClientSchedulerOptional().ifPresent(scheduler ->
                    clientTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            scheduler.tickAsync();
                        }
                    }, 1, 1)
            ).ifNotPresent(clientTimer::cancel);
            Blueberry.getEventManager().registerEvents(this, new InternalBlueberryModListener(this).createClient());
        });
        Blueberry.runOnServer(() -> Blueberry.getEventManager().registerEvents(this, new InternalBlueberryModListener(this).createServer()));
        registerArgumentTypes();
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

    private void saveConfig() {
        saveConfig(getVisualConfig());
    }

    private void saveConfig(CompoundVisualConfig config) {
        VisualConfigManager.save(getConfig(), config);
        try {
            getConfig().saveConfig();
            this.getLogger().info("Saved configuration");
        } catch (IOException ex) {
            this.getLogger().error("Could not save configuration", ex);
        }
        onReload();
    }

    private void registerItems() {
        if (InternalBlueberryModConfig.Debug.item3d) {
            BlueberryRegistries.ITEM.register(
                    "blueberry",
                    "3d",
                    new SimpleBlueberryItem(
                            this,
                            new Item.Properties()
                                    .stacksTo(1)
                                    .tab(CreativeModeTab.TAB_MISC)
                                    .rarity(Rarity.EPIC),
                            item -> new BlueberryText("blueberry", "item.blueberry.3d")
                    )
            );
        }
    }

    @Override
    public void onPostInit() {
        refreshDiscordStatus();
    }

    @Override
    public void onUnload() {
        getLogger().info("Saving config");
        saveConfig();
        getLogger().info("Stopping scheduler");
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

    @Override
    public boolean onReload() {
        // pre-reload
        try {
            getConfig().reloadConfig();
        } catch (IOException e) {
            getLogger().warn("Failed to reload config", e);
        }
        // reload
        VisualConfigManager.load(getConfig(), InternalBlueberryModConfig.class);
        // post-reload
        if (Blueberry.getSide() == Side.CLIENT) {
            if (lastDiscordRPCStatus != InternalBlueberryModConfig.Misc.DiscordRPC.status) {
                DiscordRPCTaskExecutor.shutdownNow();
                if (InternalBlueberryModConfig.Misc.DiscordRPC.status != DiscordRPCStatus.DISABLED) {
                    DiscordRPCTaskExecutor.init(InternalBlueberryModConfig.Misc.DiscordRPC.status == DiscordRPCStatus.ENABLED);
                }
            } else if (lastDiscordRPCStatus == null) {
                DiscordRPCTaskExecutor.init(InternalBlueberryModConfig.Misc.DiscordRPC.status == DiscordRPCStatus.ENABLED);
            }
            lastDiscordRPCStatus = InternalBlueberryModConfig.Misc.DiscordRPC.status;
            Minecraft mc = Minecraft.getInstance();
            //noinspection ConstantConditions
            if (mc != null) {
                mc.gui.getChat().rescaleChat();
            }
            ModState currentState = getStateList().getCurrentState();
            if (currentState == ModState.AVAILABLE || currentState == ModState.UNLOADED) {
                refreshDiscordStatus(Minecraft.getInstance().screen);
            }
        }
        return false;
    }

    public void refreshDiscordStatus() {
        if (Blueberry.getSide() != Side.CLIENT) return;
        refreshDiscordStatus(Minecraft.getInstance().screen);
    }

    public void refreshDiscordStatus(@Nullable Screen screen) {
        refreshDiscordStatus(screen, false);
    }

    public void refreshDiscordStatus(@Nullable Screen screen, boolean force) {
        if (Blueberry.getSide() != Side.CLIENT) return;
        if (!force && Objects.equals(lastScreen.get(), screen == null ? null : screen.getClass().getCanonicalName())) return;
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
            String serverIp = null;
            if (InternalBlueberryModConfig.Misc.DiscordRPC.showServerIp) serverIp = serverData.ip;
            Blueberry.getUtil().updateDiscordStatus("Connecting to server", serverIp);
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
                } else {
                    String serverIp = null;
                    if (InternalBlueberryModConfig.Misc.DiscordRPC.showServerIp) serverIp = serverData.ip;
                    Blueberry.getUtil().updateDiscordStatus("Playing on 3rd-party server", serverIp, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                }
                lastScreen.set(null);
            }
        }
    }

    public enum DiscordRPCStatus implements NameGetter {
        DISABLED,
        //ENABLED_WITHOUT_RICH_PRESENCE,
        ENABLED,
        ;

        @Contract(pure = true)
        @NotNull
        @Override
        public String getName() {
            return new BlueberryText("blueberry", "blueberry.mod.config.misc.discord_rpc.status." + name().toLowerCase(Locale.ROOT)).getContents();
        }
    }
}
