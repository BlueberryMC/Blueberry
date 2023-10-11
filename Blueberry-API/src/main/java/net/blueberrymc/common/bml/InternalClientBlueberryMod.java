package net.blueberrymc.common.bml;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class InternalClientBlueberryMod {
    private static final AtomicReference<String> LAST_SCREEN = new AtomicReference<>();

    static void doReload(@NotNull ModStateList modState, boolean forceRefreshDiscord) {
        Minecraft mc = Minecraft.getInstance();
        //noinspection ConstantConditions
        if (mc != null) {
            mc.gui.getChat().rescaleChat();
        }
        ModState currentState = modState.getCurrentState();
        if (currentState == ModState.AVAILABLE || currentState == ModState.UNLOADED) {
            InternalClientBlueberryMod.refreshDiscordStatus(Minecraft.getInstance().screen, forceRefreshDiscord);
        }
    }

    public static void refreshDiscordStatus() {
        if (Blueberry.getSide() != Side.CLIENT) return;
        refreshDiscordStatus(Minecraft.getInstance().screen);
    }

    public static void refreshDiscordStatus(@Nullable Screen screen) {
        refreshDiscordStatus(screen, false);
    }

    public static void refreshDiscordStatus(@Nullable Screen screen, boolean force) {
        if (Blueberry.getSide() != Side.CLIENT) return;
        if (!force && Objects.equals(LAST_SCREEN.get(), screen == null ? null : screen.getClass().getCanonicalName())) return;
        Minecraft minecraft = Minecraft.getInstance();
        ServerData serverData = minecraft.getCurrentServer();
        if (screen instanceof JoinMultiplayerScreen) {
            DiscordRPCTaskExecutor.destroyLobby();
            Blueberry.getUtil().updateDiscordStatus("In Server List Menu", Blueberry.getModLoader().getActiveMods().size() + " mods active");
            LAST_SCREEN.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof TitleScreen) {
            DiscordRPCTaskExecutor.destroyLobby();
            Blueberry.getUtil().updateDiscordStatus("In Main Menu", Blueberry.getModLoader().getActiveMods().size() + " mods active");
            LAST_SCREEN.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof SelectWorldScreen) {
            DiscordRPCTaskExecutor.destroyLobby();
            Blueberry.getUtil().updateDiscordStatus("In Select World Menu");
            LAST_SCREEN.set(screen.getClass().getCanonicalName());
            return;
        } else if (screen instanceof ConnectScreen && serverData != null) {
            DiscordRPCTaskExecutor.destroyLobby();
            String serverIp = null;
            if (InternalBlueberryModConfig.Misc.DiscordRPC.showServerIp) serverIp = serverData.ip;
            Blueberry.getUtil().updateDiscordStatus("Connecting to server", serverIp);
            LAST_SCREEN.set(screen.getClass().getCanonicalName());
            return;
        }
        if (screen == null) {
            LocalPlayer player = minecraft.player;
            if (player == null) {
                DiscordRPCTaskExecutor.destroyLobby();
                Blueberry.getUtil().updateDiscordStatus("In Main Menu");
                LAST_SCREEN.set(null);
                return;
            }
            IntegratedServer integratedServer = minecraft.getSingleplayerServer();
            if (minecraft.isLocalServer() && integratedServer != null) {
                Blueberry.getUtil().updateDiscordStatus("Playing on Single Player", integratedServer.getWorldData().getLevelName() + " ", BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                LAST_SCREEN.set(null);
                return;
            }
//            if (minecraft.isConnectedToRealms()) {
//                Blueberry.getUtil().updateDiscordStatus("Playing on Minecraft Realms", null, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
//                LAST_SCREEN.set(null);
//                return;
//            }
            if (serverData != null) {
                if (serverData.isLan()) {
                    Blueberry.getUtil().updateDiscordStatus("Playing on LAN server", null, BlueberryUtil.BLUEBERRY_ICON, null, System.currentTimeMillis());
                } else {
                    String serverIp = InternalBlueberryModConfig.Misc.DiscordRPC.showServerIp ? serverData.ip : null;
                    DiscordRPCTaskExecutor.createLobby().thenAccept(l ->
                            Blueberry.getUtil().updateDiscordStatus(
                                    "Playing on 3rd-party server",
                                    serverIp + " ",
                                    BlueberryUtil.BLUEBERRY_ICON,
                                    null,
                                    System.currentTimeMillis()
                            )
                    );
                }
                LAST_SCREEN.set(null);
            }
        }
    }
}
