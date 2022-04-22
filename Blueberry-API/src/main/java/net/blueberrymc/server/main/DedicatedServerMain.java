package net.blueberrymc.server.main;

import net.blueberrymc.nativeutil.NativeUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class DedicatedServerMain {
    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        File libraries = new File("libraries");
        if (libraries.exists()) {
            findJarFiles(libraries, file -> {
                System.out.println("Loading library " + libraries.toPath().relativize(file.toPath()));
                NativeUtil.appendToSystemClassLoaderSearch(file.getAbsolutePath());
            });
        }
        ServerMain.main(args);
    }

    private static void findJarFiles(File directory, Consumer<File> action) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                findJarFiles(file, action);
            } else {
                if (file.getName().endsWith(".jar")) {
                    action.accept(file);
                }
            }
        }
    }
}
