package net.blueberrymc.mixin.client.blaze3d.platform;

import com.mojang.blaze3d.platform.*;
import net.blueberrymc.client.EarlyLoadingScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public class MixinWindow {
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), method = "<init>")
    private long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        return EarlyLoadingScreen.getInstance().acquireWindowOrGet(() -> GLFW.glfwCreateWindow(width, height, title, monitor, share));
    }
}
