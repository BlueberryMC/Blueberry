package net.blueberrymc.mixin.client.blaze3d.platform;

import com.mojang.blaze3d.platform.*;
import net.blueberrymc.client.EarlyLoadingScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public class MixinWindow {
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), method = "createGlfwWindow")
    private static long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        return EarlyLoadingScreen.getInstance().acquireWindowOrGet(() -> {
            System.out.println("GLFW Platform: " + System.getenv("GLFW_PLATFORM"));
            System.out.println("__EGL_VENDOR_LIBRARY_FILENAMES: " + System.getenv("__EGL_VENDOR_LIBRARY_FILENAMES"));
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
            long window = GLFW.glfwCreateWindow(width, height, title, monitor, share);
            GLFW.glfwMakeContextCurrent(window);
            return window;
        });
    }
}
