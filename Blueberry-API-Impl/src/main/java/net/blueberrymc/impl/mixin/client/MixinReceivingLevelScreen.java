package net.blueberrymc.impl.mixin.client;

import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReceivingLevelScreen.class, remap = false)
public abstract class MixinReceivingLevelScreen {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(@NotNull CallbackInfo ci) {
        if (InternalBlueberryModConfig.Multiplayer.renderTerrainImmediately) {
            Minecraft.getInstance().setScreen(null);
            ci.cancel();
        }
    }
}
