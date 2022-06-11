package net.blueberrymc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.blueberrymc.client.util.GLUtils;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.ModState;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.InactiveProfiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_CREATION_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.GLFW_X11_CLASS_NAME;
import static org.lwjgl.glfw.GLFW.GLFW_X11_INSTANCE_NAME;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowHintString;
import static org.lwjgl.opengl.GL31C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL31C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL31C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL31C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL31C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31C.GL_FLOAT;
import static org.lwjgl.opengl.GL31C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL31C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL31C.glBindBuffer;
import static org.lwjgl.opengl.GL31C.glBindVertexArray;
import static org.lwjgl.opengl.GL31C.glBufferData;
import static org.lwjgl.opengl.GL31C.glBufferSubData;
import static org.lwjgl.opengl.GL31C.glClear;
import static org.lwjgl.opengl.GL31C.glClearColor;
import static org.lwjgl.opengl.GL31C.glDeleteBuffers;
import static org.lwjgl.opengl.GL31C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL31C.glDrawArrays;
import static org.lwjgl.opengl.GL31C.glEnable;
import static org.lwjgl.opengl.GL31C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL31C.glGenBuffers;
import static org.lwjgl.opengl.GL31C.glGenVertexArrays;
import static org.lwjgl.opengl.GL31C.glGetUniformLocation;
import static org.lwjgl.opengl.GL31C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL31C.glUseProgram;
import static org.lwjgl.opengl.GL31C.glVertexAttrib4f;
import static org.lwjgl.opengl.GL31C.glVertexAttribPointer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * This is a screen but not a screen. This is a window.
 */
public class EarlyLoadingScreen {
    public static final boolean DISABLED = Boolean.parseBoolean(System.getProperty("net.blueberrymc.client.disableEarlyLoadingScreen", "true"));
    private static final int BACKGROUND_COLOR = 0xEF323D;
    private static final int WIDTH = 854;
    private static final int HEIGHT = 480;
    private final Object LOCK = new Object();
    private final Matrix4d modelViewProjectionMatrixHUD = new Matrix4d();
    private static EarlyLoadingScreen instance;
    private long window;
    private int programId;
    private int uniformModelViewProjectionMatrixHUD;
    private int vao;
    private int vboText;
    public volatile boolean init = false;
    @NotNull
    public Consumer<Long> postTick = l -> {};
    private boolean loadingFont = false;

    public EarlyLoadingScreen() {
        instance = this;
    }

    public long getWindow() {
        return window;
    }

    public long acquireWindowOrGet(@NotNull Supplier<Long> windowSupplier) {
        if (window == 0L) return windowSupplier.get();
        _blockUntilFinish();
        return window;
    }

    public void blockUntilFinish() {
        if (DISABLED) return;
        if (!init) {
            _blockUntilFinish();
            try {
                // workaround for race condition
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            GLFW.glfwMakeContextCurrent(Minecraft.getInstance().getWindow().getWindow());
            GL.createCapabilities();
        }
    }

    private void _blockUntilFinish() {
        if (!init) {
            init = true;
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @NotNull
    public static EarlyLoadingScreen getInstance() {
        return Objects.requireNonNull(instance, "EarlyLoadingScreen not initialized yet");
    }

    public void startRender(boolean setupWindow) {
        if (DISABLED) return;
        init = false;
        long originalWindow = GLFW.glfwGetCurrentContext();
        GLFW.glfwMakeContextCurrent(0);
        if (setupWindow && !setupWindow()) {
            GLFW.glfwMakeContextCurrent(originalWindow);
            return;
        }
        Util.createThread("Render Thread - Early Loading Screen", true, () -> {
            try {
                while (!init && Blueberry.getCurrentState() != ModState.AVAILABLE) {
                    run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                glfwMakeContextCurrent(0);
                init = true;
                synchronized (LOCK) {
                    LOCK.notifyAll();
                }
            }
        }).start();
    }

    private boolean setupWindow() {
        if (!glfwInit()) return false;
        GLFWErrorCallback.createPrint(System.err).set();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        String title = "Minecraft* " + Versioning.getVersion().getGameVersion();
        glfwWindowHintString(GLFW_X11_CLASS_NAME, title);
        glfwWindowHintString(GLFW_X11_INSTANCE_NAME, title);
        window = glfwCreateWindow(WIDTH, HEIGHT, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("Could not create window");
        }

        glfwMakeContextCurrent(window);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer monPosLeft = stack.mallocInt(1);
            IntBuffer monPosTop = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            long primaryMonitor = glfwGetPrimaryMonitor();
            if (primaryMonitor != 0) {
                GLFWVidMode mode = glfwGetVideoMode(primaryMonitor);
                if (mode == null) throw new RuntimeException("Could not get Video Mode");
                glfwGetMonitorPos(primaryMonitor, monPosLeft, monPosTop);
                glfwSetWindowPos(window, (mode.width() - pWidth.get(0)) / 2 + monPosLeft.get(0), (mode.height() - pHeight.get(0)) / 2 + monPosTop.get(0));
            }
        }

        // vsync
        glfwSwapInterval(1);

        // show window
        glfwShowWindow(window);

        modelViewProjectionMatrixHUD
                .setOrtho(0.0, WIDTH, HEIGHT, 0.0, -1.0, 1.0)
                .translate(4.0, 4.0, 0.0)
                .scale(1.0, 1.0, 1.0);

        GL.createCapabilities();

        programId = GLUtils.buildShaderProgram(
                """
                        #version 330

                        uniform mat4 mMVP;

                        layout(location = 0) in vec2 iPosition;
                        layout(location = 1) in vec4 iColor;

                        out vec4 vColor;

                        void main(void) {
                            gl_Position = mMVP * vec4(iPosition, 0.0, 1.0);
                            vColor = iColor;
                        }
                        """.stripIndent(),
                """
                        #version 330

                        in vec4 vColor;

                        layout(location = 0) out vec4 oColor;

                        void main(void) {
                            oColor = vColor;
                        }
                        """.stripIndent()
        );
        uniformModelViewProjectionMatrixHUD = glGetUniformLocation(programId, "mMVP");

        glEnable(GL_CULL_FACE);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vboText = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboText);
        glBufferData(GL_ARRAY_BUFFER, WIDTH * 1024, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glfwPollEvents();
        glfwMakeContextCurrent(0);
        return true;
    }

    public void run() {
        glfwMakeContextCurrent(window);
        GLCapabilities caps = GL.createCapabilities();
        glClearColor(((BACKGROUND_COLOR >> 16) & 0xFF) / 255f, ((BACKGROUND_COLOR >> 8) & 0xFF) / 255f, (BACKGROUND_COLOR & 0xFF) / 255f, 0.0f);
        GLUtil.setupDebugMessageCallback(System.err);
        while (!init) {
            glfwMakeContextCurrent(window);
            GL.setCapabilities(caps);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderMessages(this::glRenderMessage);
            glfwSwapBuffers(window);
            glfwMakeContextCurrent(0);
            postTick.accept(window);
            try {
                //noinspection BusyWait
                Thread.sleep(25);
            } catch (InterruptedException ignore) {
                break;
            }
        }
        glfwMakeContextCurrent(window);
        glClearColor(1f, 1f, 1f, 1f);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vboText);
        glfwMakeContextCurrent(0);
    }

    public void renderMessagesFromGUI(@NotNull PoseStack poseStack) {
        blockUntilFinish();
        Minecraft mc = Minecraft.getInstance();
        Objects.requireNonNull(mc);
        boolean isFontReady = (boolean) Objects.requireNonNull(ReflectionHelper.getFieldWithoutException(LoadingOverlay.class, null, "isFontReady"));
        if (!isFontReady && !loadingFont) {
            loadingFont = true;
            // load fonts early to show logs early
            FontManager fontManager = (FontManager) ReflectionHelper.getFieldWithoutException(Minecraft.class, mc, "fontManager");
            //noinspection NullableProblems
            Objects.requireNonNull(fontManager)
                    .getReloadListener()
                    .reload(CompletableFuture::completedFuture, mc.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, Runnable::run, Runnable::run);
            isFontReady = (boolean) Objects.requireNonNull(ReflectionHelper.getFieldWithoutException(LoadingOverlay.class, null, "isFontReady"));
        }
        if (isFontReady && mc.getWindow().getWindow() == this.window) {
            renderMessages(TextRenderer.minecraft(poseStack));
        }
    }

    public void renderMessages(@NotNull TextRenderer textRenderer) {
        List<Pair<Integer, EarlyLoadingMessageManager.Message>> messages = EarlyLoadingMessageManager.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            boolean noFade = i == 0;
            final Pair<Integer, EarlyLoadingMessageManager.Message> pair = messages.get(i);
            final float fade = Mth.clamp((10000.0f - (float) pair.getFirst() - (i - 4) * 1000.0f) / 11000.0f, 0.0f, 1.0f);
            if (fade < 0.01f && !noFade) continue;
            EarlyLoadingMessageManager.Message msg = pair.getSecond();
            textRenderer.renderMessage(msg.text(), msg.type().color, ((getHeight() - 15) / 10) - i, noFade ? 1.0f : fade);
        }
        renderMemoryInfo(textRenderer);
    }

    private static final float[] MEMORY_COLOR = new float[] { 0.0f, 0.0f, 0.0f };

    private void renderMemoryInfo(@NotNull TextRenderer textRenderer) {
        final MemoryUsage hUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final MemoryUsage ohUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        final float percentage = (float) hUsage.getUsed() / hUsage.getMax();
        String memory = String.format("Memory Heap: %d / %d MB (%.1f%%)  OffHeap: %d MB", hUsage.getUsed() >> 20, hUsage.getMax() >> 20, percentage * 100.0, ohUsage.getUsed() >> 20);

        final int i = Mth.hsvToRgb((1.0f - (float) Math.pow(percentage, 1.5f)) / 3f, 1.0f, 0.5f);
        MEMORY_COLOR[2] = ((i) & 0xFF) / 255.0f;
        MEMORY_COLOR[1] = ((i >> 8) & 0xFF) / 255.0f;
        MEMORY_COLOR[0] = ((i >> 16) & 0xFF) / 255.0f;
        textRenderer.renderMessage(memory, MEMORY_COLOR, 1, 1.0f);
    }

    private void drawText(@SuppressWarnings("SameParameterValue") int x, int y, String content, int rgba) {
        glUseProgram(programId);

        try (MemoryStack frame = stackPush()) {
            glUniformMatrix4fv(uniformModelViewProjectionMatrixHUD, false, modelViewProjectionMatrixHUD.get(frame.mallocFloat(4 * 4)));
        }

        try (MemoryStack stack = stackPush()) {
            ByteBuffer text = stack.malloc(content.length() * 270);
            int quads = STBEasyFont.stb_easy_font_print(x, y, content, GLUtils.rgbaToByteBuffer(stack, GLUtils.simulateAlpha(EarlyLoadingScreen.BACKGROUND_COLOR, rgba)), text);
            text.limit(quads * 4 * 16);

            ByteBuffer triangles = memAlloc(quads * 6 * 16);
            try {
                GLUtils.copyQuadsToTriangles(text, triangles);

                glBindBuffer(GL_ARRAY_BUFFER, vboText);
                glBufferSubData(GL_ARRAY_BUFFER, 0, triangles);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, 4 * 4, 3 * 4);

                glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
                glVertexAttrib4f(1, 1, 1, 1, 1);
                glDrawArrays(GL_TRIANGLES, 0, quads * 6);
            } finally {
                memFree(triangles);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glUseProgram(0);
    }

    private interface TextRenderer {
        void renderMessage(@NotNull String message, float @NotNull [] color, int line, float alpha);

        static @NotNull TextRenderer opengl() {
            return getInstance()::glRenderMessage;
        }

        @Contract(pure = true)
        static @NotNull TextRenderer minecraft(@NotNull PoseStack poseStack) {
            return (message, color, line, alpha) -> {
                if (alpha <= 0.02) {
                    return;
                }
                int r = (int) (color[0] * 255);
                int g = (int) (color[1] * 255);
                int b = (int) (color[2] * 255);
                int a = (int) (alpha * 255);
                int rgba = (r << 16) | (g << 8) | b | (a << 24);
                Screen.drawString(poseStack, Minecraft.getInstance().font, message, 10, line * 10, rgba);
            };
        }
    }

    void glRenderMessage(@NotNull String message, float @NotNull [] color, int line, float alpha) {
        int r = (int) (color[0] * 255);
        int g = (int) (color[1] * 255);
        int b = (int) (color[2] * 255);
        int a = (int) ((1 - alpha) * 255);
        int rgba = (r << 24) | (g << 16) | (b << 8) | a;
        drawText(10, line * 10, message, rgba);
    }

    @SuppressWarnings("ConstantConditions")
    public int getHeight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            return mc.getWindow().getGuiScaledHeight();
        }
        return HEIGHT;
    }

    @SuppressWarnings("ConstantConditions")
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            return mc.getWindow().getWidth();
        }
        return WIDTH;
    }
}
