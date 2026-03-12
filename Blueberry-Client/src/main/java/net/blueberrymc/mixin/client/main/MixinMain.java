package net.blueberrymc.mixin.client.main;

import com.mojang.jtracy.TracyClient;
import net.blueberrymc.client.BlueberryClientImpl;
import net.blueberrymc.common.Blueberry;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.main.Main;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.world.level.storage.DataVersion;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

@Mixin(value = Main.class, remap = false)
public class MixinMain {
    /*
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;tryDetectVersion()V"), method = "main")
    private static void disableMethod1() {
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/jtracy/TracyClient;reportAppInfo(Ljava/lang/String;)V"), method = "main")
    private static void disableMethod2(String text) {
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;getCurrentVersion()Lnet/minecraft/WorldVersion;"), method = "main")
    private static WorldVersion disableMethod3() {
        return new WorldVersion() {
            @Override
            public @NonNull DataVersion dataVersion() {
                return new DataVersion(0, "main");
            }

            @Override
            public @NonNull String id() {
                return "";
            }

            @Override
            public @NonNull String name() {
                return "";
            }

            @Override
            public int protocolVersion() {
                return 0;
            }

            @Override
            public @NonNull PackFormat packVersion(@NonNull PackType packType) {
                return PackFormat.of(0);
            }

            @Override
            public @NonNull Date buildTime() {
                return new Date(0);
            }

            @Override
            public boolean stable() {
                return false;
            }
        };
    }
    */

    //@Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionSet;valuesOf(Ljoptsimple/OptionSpec;)Ljava/util/List;"), method = "main")
    @Inject(at = @At("HEAD"), method = "main")
    private static void enableBlueberry(String[] strings, CallbackInfo ci) {
        Blueberry.preBootstrap();
        Blueberry.bootstrap(new BlueberryClientImpl());
//        SharedConstants.tryDetectVersion();
//        TracyClient.reportAppInfo("Minecraft Java Edition " + SharedConstants.getCurrentVersion().name());
    }
}
