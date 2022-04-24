package net.blueberrymc.common.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.SafeExecutor;
import net.blueberrymc.network.CustomComponentSerializer;
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
        String locale = Blueberry.safeGetOnClient(() -> new SafeExecutor<>() {
            @NotNull
            @Override
            public String execute() {
                Minecraft mc = Minecraft.getInstance();
                //noinspection ConstantConditions // it is null before preinit
                if (mc == null) return "en_us";
                return mc.options.languageCode;
            }
        });
        if (locale != null) return locale;
        return "en_us";
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
            cache.put(cachePath, text);
        }
        String text = cache.get(cachePath);
        if (args != null && args.size() > 0) {
            text = String.format(text, args.toArray());
        }
        return text;
    }

    @NotNull
    @Override
    public BaseComponent plainCopy() {
        return new BlueberryText(this.namespace, this.path);
    }

    @NotNull
    public BlueberryText cloneWithArgs(@Nullable Object@Nullable... args) {
        return new BlueberryText(namespace, path, args);
    }

    static {
        CustomComponentSerializer.registerSerializer(BlueberryText.class, new Serializer());
    }

    public static class Serializer implements CustomComponentSerializer<BlueberryText> {
        @Override
        public @NotNull JsonElement serialize(@NotNull BlueberryText component, @NotNull JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("namespace", component.namespace);
            json.addProperty("path", component.path);
            if (component.args != null && component.args.size() > 0) {
                JsonArray array = new JsonArray();
                for (Object arg : component.args) {
                    array.add(context.serialize(arg));
                }
                json.add("args", array);
            }
            return json;
        }

        @Override
        public @NotNull BlueberryText deserialize(@NotNull JsonElement element, @NotNull JsonDeserializationContext context) {
            JsonObject json = element.getAsJsonObject();
            String namespace = json.get("namespace").getAsString();
            String path = json.get("path").getAsString();
            JsonArray array;
            if (json.has("args")) {
                array = json.get("args").getAsJsonArray();
            } else {
                array = null;
            }
            Object[] args = array == null ? null : new Object[array.size()];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    args[i] = array.get(i).getAsString();//deserializeGlobal(array.get(i), context);
                }
            }
            return new BlueberryText(namespace, path, args);
        }
    }
}
