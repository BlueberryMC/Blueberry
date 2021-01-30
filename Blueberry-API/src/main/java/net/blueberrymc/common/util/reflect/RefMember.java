package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Member;

public interface RefMember {
    @Contract(pure = true)
    @NotNull
    Member getMember();
}
