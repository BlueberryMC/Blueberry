package net.blueberrymc.impl.common.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.text.BlueberryText;
import net.blueberrymc.common.util.SafeExecutor;
import net.blueberrymc.network.CustomComponentSerializer;
import net.blueberrymc.util.Reflected;
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BlueberryTextImpl implements ComponentContents, ComponentLike, BlueberryText {
    private static final ConcurrentHashMap<String, Properties> lang = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final String namespace;
    private final String path;
    private final List<Object> args;

    @Reflected
    @Contract(pure = true)
    public BlueberryTextImpl(@NotNull String namespace, @NotNull String path, @Nullable Object@Nullable... arguments) {
        this.namespace = namespace;
        this.path = path;
        this.args = arguments != null ? Arrays.asList(arguments) : null;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Component text(@NotNull String namespace, @NotNull String path, @Nullable Object... arguments) {
        return MutableComponent.create(new BlueberryTextImpl(namespace, path, arguments));
    }

    @NotNull
    @Override
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    @Override
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
                    InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
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

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    @Override
    public BlueberryTextImpl cloneWithArgs(@Nullable Object@Nullable... args) {
        return new BlueberryTextImpl(namespace, path, args);
    }

    @Override
    public <T> @NotNull Optional<T> visit(@NotNull FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(getContents());
    }

    @Override
    public <T> @NotNull Optional<T> visit(@NotNull FormattedText.StyledContentConsumer<T> styledContentConsumer, @NotNull Style style) {
        return styledContentConsumer.accept(style, getContents());
    }

    @Override
    public String toString() {
        return "BlueberryText{" +
                "namespace='" + namespace + '\'' +
                ", path='" + path + '\'' +
                ", args=" + args +
                '}';
    }

    static {
        CustomComponentSerializer.registerSerializer(BlueberryTextImpl.class, new Serializer());
    }

    @Override
    public net.kyori.adventure.text.@NotNull Component asComponent() {
        return net.kyori.adventure.text.Component.text(getContents());
    }

    public static class Serializer implements CustomComponentSerializer<BlueberryTextImpl> {
        @Override
        public @NotNull JsonElement serialize(@NotNull BlueberryTextImpl component, @NotNull JsonSerializationContext context) {
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
        public @NotNull BlueberryTextImpl deserialize(@NotNull JsonElement element, @NotNull JsonDeserializationContext context) {
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
            return new BlueberryTextImpl(namespace, path, args);
        }
    }
}
