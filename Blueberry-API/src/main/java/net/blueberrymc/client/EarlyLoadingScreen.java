package net.blueberrymc.client;

import com.mojang.datafixers.util.Pair;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.ModState;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_CREATION_API;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_NATIVE_CONTEXT_API;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_API;
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
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowHintString;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL14.GL_CONSTANT_ALPHA;
import static org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_ALPHA;

/**
 * This is a screen but not a screen. This is a window.
 */
public class EarlyLoadingScreen {
    public static final boolean DISABLED = Boolean.parseBoolean(System.getProperty("net.blueberrymc.client.disableEarlyLoadingScreen", "true"));
    private static final int WIDTH = 854;
    private static final int HEIGHT = 480;
    private final Object LOCK = new Object();
    private static EarlyLoadingScreen instance;
    private long window;
    public volatile boolean init = false;
    @NotNull
    public Consumer<Long> postTick = l -> {};

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
                Thread.sleep(50);
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
        GLFW.glfwDefaultWindowHints();
        String title = "Minecraft* " + Versioning.getVersion().getGameVersion();
        glfwWindowHintString(GLFW_X11_CLASS_NAME, title);
        glfwWindowHintString(GLFW_X11_INSTANCE_NAME, title);
        window = glfwCreateWindow(WIDTH, HEIGHT, "Blueberry Early Loading Screen", 0, 0);
        if (window == 0) throw new RuntimeException("Could not create window");
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
        glfwShowWindow(window);
        glfwPollEvents();
        glfwMakeContextCurrent(0);
        return true;
    }

    private void setupMatrix() {
        glClear(GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, WIDTH, HEIGHT, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
    }

    public void run() {
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GLCapabilities caps = GL.createCapabilities();
        glClearColor(1f, 1f, 1f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        GLUtil.setupDebugMessageCallback(System.err);
        while (!init) {
            glfwMakeContextCurrent(window);
            GL.setCapabilities(caps);
            GL11.glPushMatrix();
            setupMatrix();
            renderBackground();
            renderMessages();
            GLFW.glfwSwapBuffers(window);
            GL11.glPopMatrix();
            glfwMakeContextCurrent(0);
            postTick.accept(window);
            try {
                //noinspection BusyWait
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
                break;
            }
        }
        glfwMakeContextCurrent(0);
    }

    private void renderBackground() {
        GL11.glBegin(GL11.GL_QUADS);
        Minecraft mc = Minecraft.getInstance();
        float r = 239 / 255f;
        float g = 50 / 255f;
        float b = 61 / 255f;
        //noinspection ConstantConditions
        if (mc != null && mc.options != null && mc.options.darkMojangStudiosBackground().get()) {
            r = g = b = 0;
        }
        GL11.glColor4f(r, g, b, 1);
        GL11.glVertex3f(0, 0, -10);
        GL11.glVertex3f(0, HEIGHT, -10);
        GL11.glVertex3f(WIDTH, HEIGHT, -10);
        GL11.glVertex3f(WIDTH, 0, -10);
        GL11.glEnd();
    }

    public void renderMessagesFromGUI() {
        blockUntilFinish();
        if (Minecraft.getInstance().getWindow().getWindow() == this.window) {
            renderMessages();
        }
    }

    public void renderMessages() {
        List<Pair<Integer, EarlyLoadingMessageManager.Message>> messages = EarlyLoadingMessageManager.getMessages();
        for (int i = 0; i < messages.size(); i++) {
            boolean noFade = i == 0;
            final Pair<Integer, EarlyLoadingMessageManager.Message> pair = messages.get(i);
            final float fade = Mth.clamp((10000.0f - (float) pair.getFirst() - (i - 4) * 1000.0f) / 11000.0f, 0.0f, 1.0f);
            if (fade < 0.01f && !noFade) continue;
            EarlyLoadingMessageManager.Message msg = pair.getSecond();
            renderMessage(msg.text(), msg.type().color, ((HEIGHT - 15) / 10) - i + 1, noFade ? 1.0f : fade);
        }
        renderMemoryInfo();
    }

    private static final float[] MEMORY_COLOR = new float[] { 0.0f, 0.0f, 0.0f };

    private void renderMemoryInfo() {
        final MemoryUsage hUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final MemoryUsage ohUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        final float percentage = (float) hUsage.getUsed() / hUsage.getMax();
        String memory = String.format("Memory Heap: %d / %d MB (%.1f%%)  OffHeap: %d MB", hUsage.getUsed() >> 20, hUsage.getMax() >> 20, percentage * 100.0, ohUsage.getUsed() >> 20);

        final int i = Mth.hsvToRgb((1.0f - (float) Math.pow(percentage, 1.5f)) / 3f, 1.0f, 0.5f);
        MEMORY_COLOR[2] = ((i) & 0xFF) / 255.0f;
        MEMORY_COLOR[1] = ((i >> 8) & 0xFF) / 255.0f;
        MEMORY_COLOR[0] = ((i >> 16) & 0xFF) / 255.0f;
        renderMessage(memory, MEMORY_COLOR, 1, 1.0f);
    }

    void renderMessage(@NotNull String message, float@NotNull[] colour, int line, float alpha) {
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        GL14.glBlendColor(0, 0, 0, alpha);
        glBlendFunc(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);
        GL11.glColor3f(colour[0], colour[1], colour[2]);
        GL11.glPushMatrix();
        GL11.glTranslatef(10, line * 10, 0);
        GL11.glScalef(1, 1, 0);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
        GL11.glPopMatrix();
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        MemoryUtil.memFree(charBuffer);
    }
}
