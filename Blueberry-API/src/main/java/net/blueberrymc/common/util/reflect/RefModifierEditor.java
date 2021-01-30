package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Modifies modifiers on Field {@link Field#modifiers}.
 * <b>Does not work on JDK 12+, since they made {@link Field#modifiers} inaccessible from the reflection.</b>
 */
@SuppressWarnings("JavadocReference")
public interface RefModifierEditor<T extends RefMember, U extends Member> extends RefMember {
    /**
     * Returns a {@link Field#modifiers} field.
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    default RefField<U> getModifiersField(@NotNull Class<U> clazz) {
        return Ref.getClass(clazz).getDeclaredField("modifiers").accessible(true);
    }

    @SuppressWarnings("unchecked")
    @Contract("-> this")
    @NotNull
    default T addFinal() {
        getModifiersField((Class<U>) this.getMember().getClass()).setObj(this.getMember(), this.getMember().getModifiers() & Modifier.FINAL);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Contract("-> this")
    @NotNull
    default T removeFinal() {
        getModifiersField((Class<U>) this.getMember().getClass()).setObj(this.getMember(), this.getMember().getModifiers() & ~Modifier.FINAL);
        return (T) this;
    }
}
