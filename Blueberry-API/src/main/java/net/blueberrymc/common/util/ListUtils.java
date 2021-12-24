package net.blueberrymc.common.util;

import net.blueberrymc.common.bml.VersionedModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ListUtils {
    /**
     * Combines all entries in provided <code>list</code> using provided <code>separator</code>. For example, if
     * <code>["a", "b", "c"]</code> was passed for <code>list</code> and <code>"-"</code> for <code>separator</code>,
     * the result would be <code>"a-b-c"</code>.
     * @param list the list
     * @param separator the separator
     * @return combined string
     */
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

    /**
     * Checks if client satisfies all requirements sent from the server.
     * @param server requirements sent from the server
     * @param client list of elements from client
     * @return true if <code>matches == server.size()</code> or <code>matches >= client.size()</code>; false otherwise.
     */
    public static <T> boolean isCompatible(@NotNull List<T> server, @NotNull List<T> client) {
        if (server.size() > client.size()) return false;
        int matches = 0;
        for (T s : server) {
            for (T c : client) {
                if (s.equals(c)) matches++;
            }
        }
        return matches == server.size()/* && matches >= client.size()*/;
    }

    /**
     * Similar to {@link #isCompatible(List, List)}, but this method compares between two VersionedModInfo.
     */
    public static boolean isCompatibleVersionedModInfo(@NotNull Collection<? extends VersionedModInfo> server, @NotNull Collection<? extends VersionedModInfo> client) {
        if (server.size() > client.size()) return false;
        int matches = 0;
        for (VersionedModInfo s : server) {
            for (VersionedModInfo c : client) {
                if (Objects.equals(s.getVersion(), c.getVersion()) && Objects.equals(s.getModId(), c.getModId()) && Objects.equals(s.getName(), c.getName())) matches++;
            }
        }
        return matches == server.size()/* && matches >= client.size()*/;
    }

    // TODO: really inefficient (client * server * 2 + client + server, so for loops 80400 if client and server has 200 mods)
    @NotNull
    public static Set<SimpleEntry<VersionedModInfo, VersionedModInfo>> getIncompatibleVersionedModInfo(@NotNull Collection<? extends VersionedModInfo> server, @NotNull Collection<? extends VersionedModInfo> client) {
        if (server.size() > client.size()) return Collections.emptySet();
        Set<SimpleEntry<VersionedModInfo, VersionedModInfo>> set = new HashSet<>();
        for (VersionedModInfo s : server) {
            VersionedModInfo sv = VersionedModInfo.copyValues(s);
            set.add(SimpleEntry.of(sv, null));
            for (VersionedModInfo c : client) {
                VersionedModInfo cv = VersionedModInfo.copyValues(c);
                set.add(SimpleEntry.of(null, cv));
            }
        }
        for (VersionedModInfo c : client) {
            set.removeIf(entry -> entry.getValue() == null && VersionedModInfo.copyValues(c).equals(entry.getKey()));
        }
        for (VersionedModInfo s : server) {
            set.removeIf(entry -> entry.getKey() == null && VersionedModInfo.copyValues(s).equals(entry.getValue()));
        }
        for (VersionedModInfo s : server) {
            for (VersionedModInfo c : client) {
                if (Objects.equals(s.getModId(), c.getModId()) && Objects.equals(s.getName(), c.getName())) {
                    if (!Objects.equals(s.getVersion(), c.getVersion())) {
                        set.removeIf(entry -> entry.getValue() == null && VersionedModInfo.copyValues(s).equals(entry.getKey()));
                        set.removeIf(entry -> entry.getKey() == null && VersionedModInfo.copyValues(c).equals(entry.getValue()));
                        set.add(SimpleEntry.of(VersionedModInfo.copyValues(s), VersionedModInfo.copyValues(c)));
                    }
                }
            }
        }
        return set;
    }
}
