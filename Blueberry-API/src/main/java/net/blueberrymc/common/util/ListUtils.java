package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListUtils {
    @NotNull
    public static String join(@NotNull List<String> list, @NotNull String separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : list) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static <T> boolean isCompatible(@NotNull List<T> server, @NotNull List<T> client) {
        if (server.size() > client.size()) return false;
        int matches = 0;
        for (T t : server) {
            for (T t1 : client) {
                if (t.equals(t1)) matches++;
            }
        }
        return matches == server.size() && matches >= client.size();
    }
}
