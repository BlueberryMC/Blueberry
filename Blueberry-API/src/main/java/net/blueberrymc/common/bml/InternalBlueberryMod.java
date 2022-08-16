package net.blueberrymc.common.bml;

import net.blueberrymc.client.command.ClientBlueberryCommand;
import net.blueberrymc.client.commands.ClientCommandManager;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfigManager;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.registry.BlueberryRegistries;
import net.blueberrymc.util.NameGetter;
import net.blueberrymc.world.item.SimpleBlueberryItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class InternalBlueberryMod extends BlueberryMod {
    private static final Timer CLIENT_TIMER = new Timer("Async Client Blueberry Scheduler", true);
    private static final Timer SERVER_TIMER = new Timer("Async Server Blueberry Scheduler", true);
    private static DiscordRPCStatus lastDiscordRPCStatus = null;

    InternalBlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        super(modLoader, description, classLoader, file);
    }

    @Override
    public void onLoad() {
        // debug
        getLogger().debug("ClassLoader: " + InternalBlueberryMod.class.getClassLoader().getClass().getTypeName());

        // discord rich presence
        DiscordRPCTaskExecutor.submit(() -> Blueberry.getUtil().updateDiscordStatus("Initializing the game"));

        // config
        onReload();
        this.setVisualConfig(VisualConfigManager.createFromClass(InternalBlueberryModConfig.class));

        // events & side-specific code
        InternalBlueberryModListener listener = new InternalBlueberryModListener(this);
        Blueberry.getEventManager().registerEvents(this, listener);
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                // config
                InternalBlueberryMod.this.getVisualConfig().onSave(InternalBlueberryMod.this::saveConfig);

                // start scheduler
                Blueberry.getUtil().getClientSchedulerOptional().ifPresent(scheduler ->
                        CLIENT_TIMER.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                scheduler.tickAsync();
                            }
                        }, 1, 1)
                ).ifNotPresent(CLIENT_TIMER::cancel);

                // register events
                Blueberry.getEventManager().registerEvents(InternalBlueberryMod.this, listener.createClient());

                // register /cblueberry client command
                ClientCommandManager.register("cblueberry", new ClientBlueberryCommand());
            }
        });
        Blueberry.runOnServer(() -> {
            // register events
            Blueberry.getEventManager().registerEvents(this, listener.createServer());
        });

        // start scheduler
        AbstractBlueberryScheduler serverScheduler = Blueberry.getUtil().getServerScheduler();
        SERVER_TIMER.scheduleAtFixedRate(new TimerTask() {
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
        registerFluids();
        registerBlocks();
        registerItems();
    }

    @Override
    public void onInit() {
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
                            item -> BlueberryText.text("blueberry", "item.blueberry.3d")
                    )
            );
        }
    }

    @Override
    public void onPostInit() {
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                InternalClientBlueberryMod.refreshDiscordStatus();
            }
        });
    }

    @Override
    public void onUnload() {
        getLogger().info("Saving config");
        saveConfig();
        getLogger().info("Stopping scheduler");
        CLIENT_TIMER.cancel();
        SERVER_TIMER.cancel();
    }

    private void registerBlocks() {
    }

    private void registerFluids() {
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
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                boolean forceRefreshDiscord = false;
                if (lastDiscordRPCStatus != InternalBlueberryModConfig.Misc.DiscordRPC.status) {
                    DiscordRPCTaskExecutor.shutdownNow();
                }
                DiscordRPCTaskExecutor.init(InternalBlueberryModConfig.Misc.DiscordRPC.status == DiscordRPCStatus.ENABLED);
                if (lastDiscordRPCStatus != DiscordRPCStatus.DISABLED) {
                    forceRefreshDiscord = true;
                }
                lastDiscordRPCStatus = InternalBlueberryModConfig.Misc.DiscordRPC.status;
                InternalClientBlueberryMod.doReload(getStateList(), forceRefreshDiscord);
            }
        });
        return false;
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
