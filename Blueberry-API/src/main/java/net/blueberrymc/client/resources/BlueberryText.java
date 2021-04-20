package net.blueberrymc.client.resources;

import net.blueberrymc.common.Blueberry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BlueberryText extends BaseComponent {
    private static final ConcurrentHashMap<String, Properties> lang = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final String namespace;
    private final String path;
    private final List<Object> args;

    public BlueberryText(@NotNull String namespace, @NotNull String path, @Nullable Object@Nullable... arguments) {
        this.namespace = namespace;
        this.path = path;
        this.args = arguments != null ? Arrays.asList(arguments) : null;
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public static String getLanguageCode() {
        return Blueberry.isServer() ? "en_us" : Minecraft.getInstance().options.languageCode;
    }

    @NotNull
    public String getPropertiesLanguageFilePath(@NotNull String code) {
        return String.format("/assets/%s/lang/%s.lang", this.namespace, code);
    }

    @NotNull
    public String getJsonLanguageFilePath(@NotNull String code) {
        return String.format("/assets/%s/lang/%s.json", this.namespace, code);
    }

    @NotNull
    public Properties getProperties(@NotNull String code) {
        String filePath = String.format("/assets/%s/lang/%s", this.namespace, code);
        if (!lang.containsKey(filePath)) {
            Properties properties = new Properties();
            String propPath = getPropertiesLanguageFilePath(code);
            InputStream in = Blueberry.class.getResourceAsStream(propPath);
            if (in == null) in = Blueberry.getModLoader().getResourceAsStream(propPath);
            if (in != null) {
                try {
                    properties.load(in);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                String jsonPath = getJsonLanguageFilePath(code);
                in = Blueberry.class.getResourceAsStream(jsonPath);
                if (in == null) in = Blueberry.getModLoader().getResourceAsStream(jsonPath);
                if (in != null) {
                    InputStreamReader reader = new InputStreamReader(in);
                    GsonHelper.parse(reader).entrySet().forEach((entry) -> {
                        String s;
                        try {
                            s = entry.getValue().getAsString();
                        } catch (UnsupportedOperationException ex) {
                            s = entry.getValue().toString();
                        }
                        properties.setProperty(entry.getKey(), s);
                    });
                }
            }
            lang.put(filePath, properties);
        }
        return lang.get(filePath);
    }

    @NotNull
    @Override
    public String getContents() {
        String cachePath = String.format("%s:%s:%s", this.namespace, this.path, getLanguageCode());
        if (!cache.containsKey(cachePath)) {
            String text = getProperties(getLanguageCode()).getProperty(path, getProperties("en_us").getProperty(path, path));
            if (args != null) {
                text = String.format(text, args.toArray());
            }
            cache.put(cachePath, text);
        }
        return cache.get(cachePath);
    }

    @NotNull
    @Override
    public BaseComponent plainCopy() {
        return new BlueberryText(this.namespace, this.path);
    }
}
