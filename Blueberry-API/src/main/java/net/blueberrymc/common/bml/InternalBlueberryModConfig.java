package net.blueberrymc.common.bml;

import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.common.bml.config.VisualConfigManager.Config;
import net.blueberrymc.common.bml.config.VisualConfigManager.DefaultValue;
import net.blueberrymc.common.bml.config.VisualConfigManager.Description;
import net.blueberrymc.common.bml.config.VisualConfigManager.HideOn;
import net.blueberrymc.common.bml.config.VisualConfigManager.Key;
import net.blueberrymc.common.bml.config.VisualConfigManager.Name;
import net.blueberrymc.common.bml.config.VisualConfigManager.Order;
import net.blueberrymc.common.bml.config.VisualConfigManager.RequiresMCRestart;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@Config
@Name("Blueberry")
public class InternalBlueberryModConfig {
    @Order(-10000)
    @Config
    @HideOn(Side.SERVER)
    @Name(namespace = "blueberry", path = "blueberry.mod.config.debug.title")
    @Key("debug")
    public static class Debug {
        @Order(-10000)
        @Config
        @Name(namespace = "blueberry", path = "blueberry.mod.config.debugRenderer.title")
        @Key("debugRenderer")
        public static class DebugRenderers {
            @Order(10000)
            @DefaultValue
            @Name("Pathfinding Renderer")
            @Key("pathfinding")
            public static boolean pathfinding = false;

            @Order(10001)
            @DefaultValue
            @Name("Water Debug Renderer")
            @Key("waterDebug")
            public static boolean waterDebug = false;

            @Order(10002)
            @DefaultValue(b = true)
            @Name("Chunk Border Renderer")
            @Key("chunkBorder")
            public static boolean chunkBorder = true;

            @Order(10003)
            @DefaultValue
            @Name("Height Map Renderer")
            @Key("heightMap")
            public static boolean heightMap = false;

            @Order(10004)
            @DefaultValue
            @Name("Collision Box Renderer")
            @Key("collisionBox")
            public static boolean collisionBox = false;

            @Order(10005)
            @DefaultValue
            @Name("Neighbors Update Renderer")
            @Key("neighborsUpdate")
            public static boolean neighborsUpdate = false;

            @Order(10006)
            @DefaultValue
            @Name("Structure Renderer")
            @Key("structure")
            public static boolean structure = false;

            @Order(10007)
            @DefaultValue
            @Name("Light Debug Renderer")
            @Key("lightDebug")
            public static boolean lightDebug = false;

            @Order(10008)
            @DefaultValue
            @Name("World Gen Attempt Renderer")
            @Key("worldGenAttempt")
            public static boolean worldGenAttempt = false;

            @Order(10009)
            @DefaultValue
            @Name("Solid Face Renderer")
            @Key("solidFace")
            public static boolean solidFace = false;

            @Order(10010)
            @DefaultValue
            @Name("Chunk Renderer")
            @Key("chunk")
            public static boolean chunk = false;

            @Order(10011)
            @DefaultValue
            @Name("Brain Debug Renderer")
            @Key("brainDebug")
            public static boolean brainDebug = false;

            @Order(10012)
            @DefaultValue
            @Name("Village Sections Debug Renderer")
            @Key("villageSectionsDebug")
            public static boolean villageSectionsDebug = false;

            @Order(10013)
            @DefaultValue
            @Name("Bee Debug Renderer")
            @Key("beeDebug")
            public static boolean beeDebug = false;

            @Order(10014)
            @DefaultValue
            @Name("Raid Debug Renderer")
            @Key("raidDebug")
            public static boolean raidDebug = false;

            @Order(10015)
            @DefaultValue
            @Name("Goal Selector Renderer")
            @Key("goalSelector")
            public static boolean goalSelector = false;

            @Order(10016)
            @DefaultValue
            @Name("Game Test Debug Renderer")
            @Key("gameTestDebug")
            public static boolean gameTestDebug = false;
        }

        @Order(10000)
        @RequiresMCRestart
        @DefaultValue
        @Name("\"3D\" Item")
        @Key("item3d")
        public static boolean item3d = false;

        @Order(10001)
        @DefaultValue
        @Name("Enable debug packets")
        @Key("debugPackets")
        public static boolean enableDebugPackets = false;

        @Order(10002)
        @DefaultValue
        @Name("Debug ModConfigScreen")
        @Description(@Name("Shows additional information on ModConfigScreen."))
        @Key("debugModConfigScreen")
        public static boolean debugModConfigScreen = false;

        @Order(10003)
        @DefaultValue
        @ApiStatus.Experimental
        @Name("Allow unloading mods")
        @Description(@Name("Adds unload button on the mods screen."))
        @Key("allowUnload")
        public static boolean allowUnload = false;

        @Order(10004)
        @Name("Numeric config control test")
        @Key("test-numeric-control")
        @Deprecated(since = "forever")
        @DeprecatedReason("deprecated because why not")
        @SideOnly(Side.CLIENT)
        public static int testNumericControl = 0;
    }

    @Order(-9999)
    @Config
    @HideOn(Side.SERVER)
    @Name(namespace = "blueberry", path = "blueberry.mod.config.multiplayer.title")
    @Key("multiplayer")
    public static class Multiplayer {
        /**
         * @deprecated Not used anymore.
         */
        @Order(10000)
        @Name(namespace = "blueberry", path = "blueberry.mod.config.multiplayer.ignore_server_view_distance")
        @Key("ignoreServerViewDistance")
        @Deprecated(forRemoval = true)
        @DeprecatedReason("Not used anymore")
        @ApiStatus.ScheduledForRemoval(inVersion = "1.6.0 or later")
        public static boolean ignoreServerViewDistance = false;

        @Order(10001)
        @Name(namespace = "blueberry", path = "blueberry.mod.config.multiplayer.render_terrain_immediately")
        @Key("renderTerrainImmediately")
        public static boolean renderTerrainImmediately = false;
    }

    @Order(-1)
    @Config
    @HideOn(Side.SERVER)
    @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.title")
    @Key("misc")
    public static class Misc {
        @Order(-10000)
        @Config
        @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.discord_rpc.title")
        @Key("discordRpc")
        public static class DiscordRPC {
            @Order(10000)
            @DefaultValue(s = "ENABLED")
            @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.discord_rpc.status")
            @Key("status")
            public static volatile InternalBlueberryMod.DiscordRPCStatus status = InternalBlueberryMod.DiscordRPCStatus.ENABLED;

            @Order(10001)
            @DefaultValue(b = true)
            @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.discord_rpc.show_server_ip")
            @Key("showServerIp")
            public static volatile boolean showServerIp = true;

            // TODO: add translation
            @Order(10002)
            @DefaultValue
            @Name("Enable game invites")
            @Description(@Name("This is an experimental feature and may be removed or changed in the future."))
            @Key("enableGameInvites")
            public static volatile boolean enableGameInvites = false;

            @Order(10003)
            @DefaultValue
            @Name("DISCORD_INSTANCE_ID") // don't translate this
            @Description(@Name("For debugging purposes. Leave empty if you are not sure what it is.")) // maybe translate this
            @Key("discordInstanceId")
            @NotNull
            public static volatile String discordInstanceId = "";
        }

        @Order(-9999)
        @Config
        @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.chat_settings.title")
        @Key("chatSettings")
        public static class ChatSettings {
            @Order(10000)
            @DefaultValue
            @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.chat_settings.extended_width")
            @Description(@Name(namespace = "blueberry", path = "blueberry.mod.config.misc.chat_settings.extended_width.description"))
            @Key("extendedWidth")
            public static boolean extendedWidth = false;

            @Order(10001)
            @DefaultValue
            @Name(namespace = "blueberry", path = "blueberry.mod.config.misc.chat_settings.extended_height")
            @Description(@Name(namespace = "blueberry", path = "blueberry.mod.config.misc.chat_settings.extended_height.description"))
            @Key("extendedHeight")
            public static boolean extendedHeight = false;
        }
    }

    @Config
    @HideOn(Side.CLIENT)
    @Name("Server")
    @Key("server")
    public static class Server {
        @RequiresMCRestart
        @DefaultValue
        @Name("BungeeCord")
        @Key("bungeecord")
        public static boolean bungee = false;
    }
}
