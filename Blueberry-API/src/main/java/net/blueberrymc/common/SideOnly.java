package net.blueberrymc.common;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an element that the element is only available on the specific side. If value is {@link Side#CLIENT} and the
 * runtime environment is a server, the element will be ignored on the server. This means methods/fields will not be
 * available, and attempting to load client-only class will result in an exception. Using {@link Side#BOTH} for value
 * is not supported.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface SideOnly {
    @NotNull Side value();
}
