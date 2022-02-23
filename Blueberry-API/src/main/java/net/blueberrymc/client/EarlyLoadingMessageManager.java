package net.blueberrymc.client;

import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class EarlyLoadingMessageManager {
    private static volatile EnumMap<MessageType, List<Message>> messages = new EnumMap<>(MessageType.class);

    @NotNull
    public static List<Pair<Integer, Message>> getMessages() {
        final long ts = System.nanoTime();
        return messages.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingLong(Message::timestamp).thenComparing(Message::text).reversed())
                .map(m -> Pair.of((int) ((ts - m.timestamp) / 1000000), m))
                .limit(25)
                .collect(Collectors.toList());
    }

    public record Message(@NotNull String text, @NotNull MessageType type, long timestamp) {}

    public enum MessageType {
        ERROR(0.4f, 0.0f, 0.0f),
        WARNING(0.6f, 0.6f, 0.0f),
        MINECRAFT(1.0f, 1.0f, 1.0f),
        MOD_LOADER(0.0f, 0.0f, 0.5f),
        MOD_COMPILER(0.5f, 0.0f, 0.5f),
        MOD(0.5f, 0.0f, 0.0f);

        public final float[] color;

        MessageType(final float r, final float g, final float b) {
            color = new float[] { r, g, b };
        }
    }

    private static boolean isMainThread() {
        return Thread.currentThread().getId() == 1;
    }

    synchronized static void addMessage(@NotNull MessageType type, @NotNull String message, int maxSize) {
        if (isMainThread()) GLFW.glfwPollEvents();
        EnumMap<MessageType, List<Message>> newMessages = new EnumMap<>(messages);
        newMessages.compute(type, (key, existingList) -> {
            List<Message> newList = new ArrayList<>();
            if (existingList != null) {
                if (maxSize < 0) {
                    newList.addAll(existingList);
                } else {
                    newList.addAll(existingList.subList(0, Math.min(existingList.size(), maxSize)));
                }
            }
            newList.add(new Message(message, type, System.nanoTime()));
            return newList;
        });
        messages = newMessages;
    }

    public static void logMinecraft(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.MINECRAFT, safeMessage, -1);
    }

    public static void logModLoader(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.MOD_LOADER, safeMessage, -1);
    }

    public static void logModCompiler(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.MOD_COMPILER, safeMessage, -1);
    }

    public static void logMod(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.MOD, safeMessage, 20);
    }

    public static void logWarning(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.WARNING, safeMessage, -1);
    }

    public static void logError(@NotNull String message) {
        String safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 150, "...");
        addMessage(MessageType.ERROR, safeMessage, -1);
    }
}
