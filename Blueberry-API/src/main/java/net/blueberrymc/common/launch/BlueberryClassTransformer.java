package net.blueberrymc.common.launch;

import net.blueberrymc.common.util.BlueberryEvil;
import net.blueberrymc.util.SimpleRecursionDetector;
import net.minecraft.launchwrapper.IClassTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused") // see BlueberryTweaker
public class BlueberryClassTransformer implements IClassTransformer {
    private static final Set<String> TRANSFORMER_EXCLUDES = new HashSet<>(Arrays.asList(
            "net.blueberrymc.common.util.BlueberryEvil",
            "net.blueberrymc.common.util.AnnotationRecorder"
    ));
    private final SimpleRecursionDetector recursionDetector = new SimpleRecursionDetector();

    @Override
    public byte@Nullable[] transform(@NotNull String className, @NotNull String transformedName, byte@Nullable[] bytes) {
        if (bytes == null) return null; // we are not class generator
        for (String transformerExclude : TRANSFORMER_EXCLUDES) {
            if (className.startsWith(transformerExclude)) {
                return bytes;
            }
        }
        recursionDetector.push();
        try {
            return BlueberryEvil.convert(className, bytes);
        } finally {
            recursionDetector.pop();
        }
    }
}
