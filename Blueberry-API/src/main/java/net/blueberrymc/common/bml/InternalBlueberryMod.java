package net.blueberrymc.common.bml;

import net.blueberrymc.client.command.ClientBlueberryCommand;
import net.blueberrymc.client.commands.ClientCommandManager;
import net.blueberrymc.command.argument.ModIdArgument;
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
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
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
    private static final Timer clientTimer = new Timer("Async Client Blueberry Scheduler", true);
    private static final Timer serverTimer = new Timer("Async Server Blueberry Scheduler", true);
    private static DiscordRPCStatus lastDiscordRPCStatus = null;

    InternalBlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        super(modLoader, description, classLoader, file);
    }

    @Override
    public void onLoad() {
        getLogger().debug("ClassLoader: " + InternalBlueberryMod.class.getClassLoader().getClass().getTypeName());
        Blueberry.getUtil().updateDiscordStatus("Initializing the game", getStateList().getCurrentState().getName());
        onReload();
        this.setVisualConfig(VisualConfigManager.createFromClass(InternalBlueberryModConfig.class));
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                InternalBlueberryMod.this.getVisualConfig().onSave(InternalBlueberryMod.this::saveConfig);
                Blueberry.getUtil().getClientSchedulerOptional().ifPresent(scheduler ->
                        clientTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                scheduler.tickAsync();
                            }
                        }, 1, 1)
                ).ifNotPresent(clientTimer::cancel);
                Blueberry.getEventManager().registerEvents(InternalBlueberryMod.this, new InternalBlueberryModListener(InternalBlueberryMod.this).createClient());
                ClientCommandManager.register("cblueberry", new ClientBlueberryCommand());
            }
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
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                if (lastDiscordRPCStatus != InternalBlueberryModConfig.Misc.DiscordRPC.status) {
                    DiscordRPCTaskExecutor.shutdownNow();
                    if (InternalBlueberryModConfig.Misc.DiscordRPC.status != DiscordRPCStatus.DISABLED) {
                        DiscordRPCTaskExecutor.init(InternalBlueberryModConfig.Misc.DiscordRPC.status == DiscordRPCStatus.ENABLED);
                    }
                } else if (lastDiscordRPCStatus == null) {
                    DiscordRPCTaskExecutor.init(InternalBlueberryModConfig.Misc.DiscordRPC.status == DiscordRPCStatus.ENABLED);
                }
                lastDiscordRPCStatus = InternalBlueberryModConfig.Misc.DiscordRPC.status;
                InternalClientBlueberryMod.doReload(getStateList());
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
