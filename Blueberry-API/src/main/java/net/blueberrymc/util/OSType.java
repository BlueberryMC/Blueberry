package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

public enum OSType {
    Linux,
    Mac_OS,
    Mac_OS_X,
    Windows,
    Solaris,
    FreeBSD,
    Unknown,
    ;

    @NotNull
    public static OSType detectOS() {
        String os = System.getProperty("os.name");
        if (os == null) return Unknown;
        if (os.equals("Linux")) return Linux;
        if (os.equals("Mac OS")) return Mac_OS;
        if (os.equals("Mac OS X")) return Mac_OS_X;
        if (os.contains("Windows")) return Windows;
        if (os.equals("Solaris")) return Solaris;
        if (os.equals("FreeBSD")) return FreeBSD;
        return Unknown;
    }
}
