package net.blueberrymc.mixin.client.gui.font;

import net.blueberrymc.client.BlueberryClient;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FontManager.class, remap = true)
public class MixinFontManager {
    @Inject(at = @At("TAIL"), method = "apply")
    public void setFontReady(FontManager.Preparation preparation, ProfilerFiller profilerFiller, CallbackInfo ci) {
        BlueberryClient.isFontReady = true;
    }
}
