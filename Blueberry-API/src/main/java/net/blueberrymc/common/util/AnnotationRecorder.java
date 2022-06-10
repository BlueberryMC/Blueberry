package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AnnotationRecorder {
    @NotNull
    public static AnnotationVisitResult visitAnnotations(@NotNull ClassReader cr) {
        // Result instance to save the result
        AnnotationVisitResult result = AnnotationVisitResult.createMutable();

        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @SuppressWarnings("DuplicatedCode") // not many options here :(
            @Override
            @NotNull
            public MethodVisitor visitMethod(int access, @NotNull String name, @NotNull String descriptor, @Nullable String signature, @NotNull String @Nullable [] exceptions) {
                // Create new member
                AnnotatedMember member = AnnotatedMember.createMutable(new Member(Type.METHOD, name, descriptor));

                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    @NotNull
                    public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
                        return new Visitor(descriptor, data -> member.data.add(data.immutable()));
                    }

                    @Override
                    public void visitEnd() {
                        result.members.add(member.immutable());
                        super.visitEnd();
                    }
                };
            }

            @SuppressWarnings("DuplicatedCode") // not many options here :(
            @Override
            @NotNull
            public FieldVisitor visitField(int access, @NotNull String name, @NotNull String descriptor, @Nullable String signature, @Nullable Object value) {
                // Create new member
                AnnotatedMember member = AnnotatedMember.createMutable(new Member(Type.FIELD, name, descriptor));

                return new FieldVisitor(Opcodes.ASM9) {
                    @Override
                    @NotNull
                    public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
                        return new Visitor(descriptor, data -> member.data.add(data.immutable()));
                    }

                    @Override
                    public void visitEnd() {
                        result.members.add(member.immutable());
                        super.visitEnd();
                    }
                };
            }

            @Override
            @NotNull
            public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
                return new Visitor(descriptor, data -> result.classAnnotations.add(data.immutable()));
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        // now we just need to return the (immutable) result
        return result.immutable();
    }

    private static class Visitor extends AnnotationVisitor {
        private final AnnotationData data;
        private final Consumer<@NotNull AnnotationData> onEnd;

        public Visitor(@NotNull String type, @NotNull Consumer<@NotNull AnnotationData> onEnd) {
            super(Opcodes.ASM9);
            this.data = AnnotationData.createMutable(type);
            this.onEnd = onEnd;
        }

        @Override
        public void visit(@NotNull String name, @NotNull Object value) {
            data.parameters.add(AnnotationParameter.createPrimitive(name, value));
            super.visit(name, value);
        }

        @Override
        public void visitEnum(@NotNull String name, @NotNull String descriptor, @NotNull String value) {
            data.parameters.add(AnnotationParameter.createEnum(name, descriptor, value));
            super.visitEnum(name, descriptor, value);
        }

        @Nullable
        @Override
        public AnnotationVisitor visitArray(@NotNull String name) {
            data.parameters.add(AnnotationParameter.createOther(name));
            return null;
        }

        @Nullable
        @Override
        public AnnotationVisitor visitAnnotation(@NotNull String name, @NotNull String descriptor) {
            data.parameters.add(AnnotationParameter.createOther(name));
            return null;
        }

        @Override
        public void visitEnd() {
            onEnd.accept(data.immutable());
            super.visitEnd();
        }
    }

    public record AnnotationVisitResult(@NotNull List<AnnotatedMember> members, @NotNull List<AnnotationData> classAnnotations) implements IAnnotatedMember {
        @Contract(" -> new")
        private static @NotNull AnnotationVisitResult createMutable() {
            return new AnnotationVisitResult(new ArrayList<>(), new ArrayList<>());
        }

        @Contract(" -> new")
        public @NotNull AnnotationVisitResult immutable() {
            return new AnnotationVisitResult(Collections.unmodifiableList(members), Collections.unmodifiableList(classAnnotations));
        }

        public @Nullable AnnotatedMember findMember(@NotNull Type type, @NotNull String name, @NotNull String desc) {
            for (AnnotatedMember member : members) {
                if (member.member.type == type && member.member.name.equals(name) && member.member.desc.equals(desc)) {
                    return member;
                }
            }
            return null;
        }

        @Contract(pure = true)
        @Override
        public @NotNull List<AnnotationData> getAnnotations() {
            return classAnnotations;
        }
    }

    public record AnnotatedMember(@NotNull Member member, @NotNull List<AnnotationData> data) implements IAnnotatedMember {
        @Contract("_ -> new")
        public static @NotNull AnnotatedMember createMutable(@NotNull Member member) {
            return new AnnotatedMember(member, new ArrayList<>());
        }

        @Contract(" -> new")
        public @NotNull AnnotatedMember immutable() {
            return new AnnotatedMember(member, Collections.unmodifiableList(data));
        }

        @Contract(pure = true)
        @Override
        public @NotNull List<AnnotationData> getAnnotations() {
            return data;
        }
    }

    public interface IAnnotatedMember {
        @NotNull
        List<AnnotationData> getAnnotations();

        @Contract(pure = true)
        default boolean isAnnotated(@NotNull String type) {
            for (AnnotationData annotation : getAnnotations()) {
                if (annotation.type.equals(type)) {
                    return true;
                }
            }
            return false;
        }

        @Contract(pure = true)
        default @Nullable AnnotationData findAnnotation(@NotNull String type) {
            for (AnnotationData annotation : getAnnotations()) {
                if (annotation.type.equals(type)) {
                    return annotation;
                }
            }
            return null;
        }
    }

    public record AnnotationData(@NotNull String type, @NotNull List<AnnotationParameter> parameters) {
        @Contract("_ -> new")
        public static @NotNull AnnotationData createMutable(@NotNull String type) {
            return new AnnotationData(type, new ArrayList<>());
        }

        @Contract(" -> new")
        public @NotNull AnnotationData immutable() {
            return new AnnotationData(type, Collections.unmodifiableList(parameters));
        }

        @Contract(pure = true)
        public @Nullable AnnotationParameter findParameter(@NotNull String name) {
            for (AnnotationParameter parameter : parameters) {
                if (parameter.name.equals(name)) {
                    return parameter;
                }
            }
            return null;
        }
    }

    public record AnnotationParameter(@NotNull String name, @NotNull AnnotationParameterValue value) {
        @Contract("_, _ -> new")
        public static @NotNull AnnotationParameter createPrimitive(@NotNull String name, @NotNull Object value) {
            return new AnnotationParameter(name, new AnnotationParameterPrimitive(value));
        }

        @Contract("_, _, _ -> new")
        public static @NotNull AnnotationParameter createEnum(@NotNull String name, @NotNull String enumType, @NotNull String value) {
            return new AnnotationParameter(name, new AnnotationParameterEnum(enumType, value));
        }

        @Contract("_, _ -> new")
        public static @NotNull AnnotationParameter createOther(@NotNull String name, @NotNull Object @NotNull ... value) {
            return new AnnotationParameter(name, new AnnotationParameterOther(value));
        }
    }

    public interface AnnotationParameterValue {}

    public record AnnotationParameterPrimitive(@NotNull Object value) implements AnnotationParameterValue {}

    public record AnnotationParameterEnum(@NotNull String enumType, @NotNull String value) implements AnnotationParameterValue {}

    public record AnnotationParameterOther(@NotNull Object @NotNull ... value) implements AnnotationParameterValue {}

    public record Member(@NotNull Type type, @NotNull String name, @NotNull String desc) {}

    public enum Type {
        METHOD,
        FIELD,
    }
}
