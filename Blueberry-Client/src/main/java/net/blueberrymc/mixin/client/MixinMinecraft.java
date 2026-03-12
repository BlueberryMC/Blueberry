package net.blueberrymc.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.blueberrymc.client.EarlyLoadingScreen;
import net.blueberrymc.client.event.ClientEventFactory;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.client.gui.screens.ModLoadingProblemScreen;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.common.util.Versioning;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;
import java.util.function.Function;

@Mixin(value = Minecraft.class)
public class MixinMinecraft {
    @Shadow
    @Nullable
    public Screen screen;

    @Redirect(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 2), method = "<init>")
    public void startRenderEarlyLoadingScreen(Logger instance, String s, Object o) {
        instance.info(s, o);
        EarlyLoadingScreen.getInstance().startRender(false);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"), method = "<init>")
    public void startPreInit(PackRepository instance) {
        Blueberry.getModLoader().callPreInit();
        instance.reload();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"), method = "<init>")
    public void blockUntilFinish(ReloadableResourceManager instance, PreparableReloadListener preparableReloadListener) {
        instance.registerReloadListener(preparableReloadListener);
        EarlyLoadingScreen.getInstance().blockUntilFinish();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ResourceLoadStateTracker;startReload(Lnet/minecraft/client/ResourceLoadStateTracker$ReloadReason;Ljava/util/List;)V"), method = "<init>")
    public void startInit(ResourceLoadStateTracker instance, ResourceLoadStateTracker.ReloadReason reloadReason, List<PackResources> list) {
        Blueberry.getModLoader().callInit();
        instance.startReload(reloadReason, list);
    }

    @Inject(at = @At("TAIL"), method = "addInitialScreens")
    public void addModLoadingProblemScreen(List<Function<Runnable, Screen>> screens, CallbackInfoReturnable<Boolean> cir) {
        if (ModLoadingErrors.hasErrorOrWarning()) {
            screens.add(ModLoadingProblemScreen::new);
        }
    }

    @Inject(at = @At("HEAD"), method = "crash")
    private static void shutdownDiscordRpcOnCrash(Minecraft minecraft, File file, CrashReport crashReport, CallbackInfo ci) {
        DiscordRPCTaskExecutor.shutdownNow();
    }

    @Inject(at = @At("TAIL"), method = "setScreen")
    public void callScreenChangedEvent(Screen screen, CallbackInfo ci) {
        ClientEventFactory.callScreenChangedEvent(this.screen);
    }

    @Inject(at = @At("TAIL"), method = "setOverlay")
    public void callOverlayChangedEvent(Overlay overlay, CallbackInfo ci) {
        ClientEventFactory.callOverlayChangedEvent(overlay);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;close()V"), method = "destroy")
    public void destroyBlueberry(CallbackInfo ci) {
        Blueberry.shutdown();
    }

    @Inject(at = @At("TAIL"), method = "tick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void postTick(CallbackInfo ci, ProfilerFiller profiler) {
        profiler.push("blueberryClientScheduler");
        Blueberry.getUtil().getClientScheduler().tick();
        profiler.pop();
    }

    @Inject(at = @At("RETURN"), method = "getLaunchedVersion", cancellable = true)
    public void getLaunchedVersion(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(Versioning.getVersion().getFullyQualifiedVersion());
    }
}
