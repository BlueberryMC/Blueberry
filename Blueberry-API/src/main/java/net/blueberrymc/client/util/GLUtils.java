package net.blueberrymc.client.util;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memCopy;

public class GLUtils {
    public static int buildShaderProgram(String vsh, String fsh) {
        int vshader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vshader, vsh);
        glCompileShader(vshader);
        if (glGetShaderi(vshader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException(glGetShaderInfoLog(vshader));
        }

        int fshader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fshader, fsh);
        glCompileShader(fshader);
        if (glGetShaderi(fshader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException(glGetShaderInfoLog(fshader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException(glGetProgramInfoLog(program));
        }

        glDeleteShader(fshader);
        glDeleteShader(vshader);

        return program;
    }

    public static ByteBuffer copyQuadsToTriangles(ByteBuffer quads, ByteBuffer triangles) {
        int vertexCount = quads.remaining() >> 4;
        int quadCount   = vertexCount >> 2;

        if (triangles == null) {
            triangles = memAlloc(quadCount * 6 * 16);
        }

        long s = memAddress(quads);
        long t = memAddress(triangles);

        for (int i = 0; i < quadCount; i++) {
            long quad = s + i * (4 * 16);

            long triangle = t + i * (6 * 16);

            memCopy(quad, triangle, 16);
            memCopy(quad + 2 * 16, triangle + 16, 16);
            memCopy(quad + 16, triangle + 2 * 16, 16);

            memCopy(quad, triangle + 3 * 16, 16);
            memCopy(quad + 3 * 16, triangle + 4 * 16, 16);
            memCopy(quad + 2 * 16, triangle + 5 * 16, 16);
        }

        return triangles;
    }

    public static ByteBuffer rgbaToByteBuffer(MemoryStack stack, int rgba) {
        ByteBuffer buffer = stack.malloc(4);
        buffer.put((byte) ((rgba >> 24) & 0xFF));
        buffer.put((byte) ((rgba >> 16) & 0xFF));
        buffer.put((byte) ((rgba >> 8) & 0xFF));
        buffer.put((byte) (rgba & 0xFF));
        buffer.flip();
        return buffer;
    }

    public static int simulateAlpha(int backgroundColor, int rgba) {
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        int a = rgba & 0xFF;
        int br = (backgroundColor >> 16) & 0xFF;
        int bg = (backgroundColor >> 8) & 0xFF;
        int bb = backgroundColor & 0xFF;
        double nonAlphaPercentage = (0xFF - a) / 255.0;
        int newR = (int) (br + (r - br) * nonAlphaPercentage);
        int newG = (int) (bg + (g - bg) * nonAlphaPercentage);
        int newB = (int) (bb + (b - bb) * nonAlphaPercentage);
        return (newR << 24) | (newG << 16) | (newB << 8) | a;
    }
}
