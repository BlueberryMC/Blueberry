package net.blueberrymc.common.bml.config;

import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.config.ModConfig;
import net.blueberrymc.config.yaml.YamlObject;
import net.blueberrymc.util.Util;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VisualConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private static Component tryGetComponent(BlueberryMod mod, Name configName) {
        if (configName != null) {
            String namespace = configName.namespace();
            String path = configName.path();
            String name = configName.value();
            if (namespace.length() > 0 && path.length() > 0) {
                return new BlueberryText(namespace, path);
            }
            if (namespace.length() == 0 && path.length() > 0) {
                String theNamespace;
                if (mod == null) {
                    theNamespace = "minecraft";
                } else {
                    theNamespace = mod.getModId();
                }
                return new BlueberryText(theNamespace, path);
            }
            if (namespace.length() == 0 && name.length() > 0) {
                return new TextComponent(name);
            }
        }
        if (mod != null) return new TextComponent(mod.getName());
        return null;
    }

    /**
     * Creates VisualConfig from class.
     * <p>How this method decides compound name:
     * <ol>
     *     <li>@Config(name = ...) parameter</li>
     *     <li>Mod name (from class loader)</li>
     *     <li>Class name ({@link Class#getSimpleName()})</li>
     * </ol>
     * @param clazz the class (must be annotated with @Config)
     * @return created compound config
     * @throws IllegalArgumentException when @Config annotation is missing
     * @throws IllegalArgumentException when class is not public
     */
    @NotNull
    public static RootCompoundVisualConfig createFromClass(@NotNull Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Config.class)) throw new IllegalArgumentException("@Config annotation is missing on class " + clazz.getTypeName());
        //Config config = clazz.getAnnotation(Config.class);
        BlueberryMod mod = BlueberryMod.detectModFromClass(clazz);
        Component component = tryGetComponent(mod, clazz.getAnnotation(Name.class));
        if (component == null) component = new TextComponent(clazz.getSimpleName());
        return createFromClass(mod, component, clazz);
    }

    /**
     * Creates VisualConfig from class. <code>onChanged</code> handler will be invoked with following order:
     * <ol>
     *     <li>Set values into fields</li>
     *     <li>Invoke all saveHandlers</li>
     *     <li>Invoke <code>super.onChanged()</code></li>
     * </ol>
     * Supported field types:
     * <ul>
     *     <li>boolean</li>
     *     <li>AtomicBoolean</li>
     *     <li>int</li>
     *     <li>AtomicInteger</li>
     *     <li>Enum</li>
     * </ul>
     * @param clazz the class (must be annotated with @Config)
     * @return created compound config
     * @throws IllegalArgumentException when class is not public
     */
    @NotNull
    public static RootCompoundVisualConfig createFromClass(@Nullable BlueberryMod mod, @NotNull Component name, @NotNull Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) throw new IllegalArgumentException("Class must be public");
        List<Runnable> saveHandlers = new ArrayList<>();
        List<Map.Entry<VisualConfig<?>, AnnotatedElement>> visited = new ArrayList<>();
        RootCompoundVisualConfig root = new RootCompoundVisualConfig(name) {
            // override onChanged method so developers can use #onSave to listen for config changes
            @Override
            public void onChanged() {
                List<Map.Entry<VisualConfig<?>, AnnotatedElement>> entries = new ArrayList<>();
                for (Map.Entry<VisualConfig<?>, AnnotatedElement> entry : visited) {
                    VisualConfig<?> config = entry.getKey();
                    if (entry.getValue() instanceof Field field) {
                        if (field.toGenericString().equals(config.getId())) {
                            entries.add(new AbstractMap.SimpleImmutableEntry<>(config, field));
                            if (field.getType() == AtomicBoolean.class) {
                                Objects.requireNonNull(getField(AtomicBoolean.class, field)).set((boolean) config.get());
                            } else if (field.getType() == AtomicInteger.class) {
                                Objects.requireNonNull(getField(AtomicInteger.class, field)).set((int) config.get());
                            } else if (field.getType() == AtomicLong.class) {
                                Objects.requireNonNull(getField(AtomicLong.class, field)).set((long) config.get());
                            } else {
                                setField(field, config.get());
                            }
                        }
                    }
                }
                saveHandlers.forEach(Runnable::run);
                super.onChanged();
                entries.forEach(entry -> {
                    if (entry.getValue() instanceof Field f) {
                        entry.getKey().id(f.toGenericString());
                    } else if (entry.getValue() instanceof Class<?> c) {
                        entry.getKey().id(c.getTypeName());
                    }
                });
            }
        };
        root.id(getKey(clazz));
        List<Map.Entry<Integer, AnnotatedElement>> things = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (!field.isAnnotationPresent(Name.class)) continue;
            if (shouldSkip(field)) continue;
            things.add(new AbstractMap.SimpleImmutableEntry<>(getOrder(field), field));
        }
        for (Class<?> c : clazz.getClasses()) {
            if (!c.isAnnotationPresent(Config.class)) continue;
            if (shouldSkip(c)) continue;
            things.add(new AbstractMap.SimpleImmutableEntry<>(getOrder(c), c));
        }
        things.sort(Comparator.comparingInt(Map.Entry::getKey));
        things.forEach(entry -> {
            AnnotatedElement element = entry.getValue();
            if (element instanceof Field field) {
                Name config = field.getAnnotation(Name.class);
                if (field.getType() == boolean.class) {
                    var cfg = new BooleanVisualConfig(tryGetComponent(mod, config), getField(boolean.class, field), (Boolean) getDefaultValue(field))
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == AtomicBoolean.class) {
                    var cfg = new BooleanVisualConfig(tryGetComponent(mod, config), Objects.requireNonNull(getField(AtomicBoolean.class, field)).get(), (Boolean) getDefaultValue(field))
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == int.class) {
                    ParamInteger param = field.getAnnotation(ParamInteger.class);
                    int min = Integer.MIN_VALUE;
                    int max = Integer.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new IntegerVisualConfig(tryGetComponent(mod, config), getField(int.class, field), (Integer) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == AtomicInteger.class) {
                    ParamInteger param = field.getAnnotation(ParamInteger.class);
                    int min = Integer.MIN_VALUE;
                    int max = Integer.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new IntegerVisualConfig(tryGetComponent(mod, config), Objects.requireNonNull(getField(AtomicInteger.class, field)).get(), (Integer) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == long.class) {
                    ParamLong param = field.getAnnotation(ParamLong.class);
                    long min = Long.MIN_VALUE;
                    long max = Long.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new LongVisualConfig(tryGetComponent(mod, config), getField(long.class, field), (Long) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == AtomicLong.class) {
                    ParamLong param = field.getAnnotation(ParamLong.class);
                    long min = Long.MIN_VALUE;
                    long max = Long.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new LongVisualConfig(tryGetComponent(mod, config), Objects.requireNonNull(getField(AtomicLong.class, field)).get(), (Long) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == double.class) {
                    ParamDouble param = field.getAnnotation(ParamDouble.class);
                    double min = Double.MIN_VALUE;
                    double max = Double.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new DoubleVisualConfig(tryGetComponent(mod, config), getField(double.class, field), (Double) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == float.class) {
                    ParamFloat param = field.getAnnotation(ParamFloat.class);
                    float min = Float.MIN_VALUE;
                    float max = Float.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new FloatVisualConfig(tryGetComponent(mod, config), getField(float.class, field), (Float) getDefaultValue(field), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == byte.class) {
                    ParamByte param = field.getAnnotation(ParamByte.class);
                    byte min = Byte.MIN_VALUE;
                    byte max = Byte.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new ByteVisualConfig(tryGetComponent(mod, config), getField(byte.class, field), ((Number) getDefaultValue(field)).byteValue(), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == short.class) {
                    ParamShort param = field.getAnnotation(ParamShort.class);
                    short min = Short.MIN_VALUE;
                    short max = Short.MAX_VALUE;
                    if (param != null) {
                        min = param.min();
                        max = param.max();
                    }
                    var cfg = new ShortVisualConfig(tryGetComponent(mod, config), getField(short.class, field), ((Number) getDefaultValue(field)).shortValue(), min, max)
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == String.class) {
                    var cfg = new StringVisualConfig(tryGetComponent(mod, config), getField(String.class, field), (String) getDefaultValue(field))
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType() == Class.class) {
                    var cfg = new ClassVisualConfig(tryGetComponent(mod, config), getField(Class.class, field), (Class<?>) getDefaultValue(field))
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                } else if (field.getType().isEnum()) {
                    var cfg = CycleVisualConfig.fromEnumUnchecked(tryGetComponent(mod, config), field.getType(), getField(Object.class, field), getDefaultValue(field))
                            .reverse(shouldReverse(field))
                            .id(getKey(field))
                            .description(getDescription(field))
                            .requiresRestart(requiresMCRestart(field));
                    visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, field));
                    root.add(cfg);
                }
                // TODO: find a good way to implement CycleVisualConfig of List
            } else if (element instanceof Class<?> c) {
                var cfg = createFromClass(c);
                visited.add(new AbstractMap.SimpleImmutableEntry<>(cfg, c));
                root.add(cfg);
            }
        });
        // add save handlers
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getParameterCount() != 0) continue;
            if (method.getReturnType() != void.class) continue;
            if (!method.isAnnotationPresent(SaveHandler.class)) continue;
            saveHandlers.add(() -> {
                try {
                    method.invoke(null);
                } catch (ReflectiveOperationException ex) {
                    LOGGER.warn("Could not invoke save handler" , ex);
                }
            });
        }
        return root;
    }

    /**
     * Saves the configuration into the provided mod config.
     * @see #load(ModConfig, Class)
     */
    public static void save(@NotNull ModConfig config, @NotNull CompoundVisualConfig compound) {
        save(true, config.getConfig(), compound);
    }

    private static void save(boolean root, @NotNull YamlObject obj, @NotNull CompoundVisualConfig compound) {
        YamlObject newObj;
        if (root) {
            newObj = obj;
        } else {
            if (compound.getId() == null) return;
            newObj = Objects.requireNonNullElseGet(obj.getObject(compound.getId()), YamlObject::new);
        }
        for (VisualConfig<?> cfg : compound) {
            if (cfg instanceof CompoundVisualConfig nestedConfig) {
                save(false, newObj, nestedConfig);
            } else {
                if (cfg.getId() != null) {
                    Object o = cfg.get();
                    if (o instanceof Class<?> clazz) o = clazz.getTypeName();
                    if (o instanceof Enum<?> e) o = e.name();
                    newObj.setNullable(cfg.getId(), o);
                }
            }
        }
        if (!root) obj.setObject(compound.getId(), newObj);
    }

    /**
     * Loads the configuration into config class from the provided mod config.
     * @throws IllegalArgumentException when class is not annotated with @{@link Config Config}
     * @see #save(ModConfig, CompoundVisualConfig)
     */
    public static void load(@NotNull ModConfig config, @NotNull Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Name.class)) throw new IllegalArgumentException("Class " + clazz.getTypeName() + " is not annotated with @Config");
        load(config.getConfig(), clazz);
    }

    private static void load(@NotNull YamlObject obj, @NotNull Class<?> clazz) {
        for (Field field : clazz.getFields()) {
            if (!field.isAnnotationPresent(Name.class)) continue;
            if (shouldSkip(field)) continue;
            String key = getKey(field);
            if (key == null) continue;
            if (!obj.getRawData().containsKey(key)) continue;
            if (field.getType() == boolean.class) {
                setField(field, obj.getBoolean(key, (boolean) Util.getOrDefault(getDefaultValue(field), false)));
            } else if (field.getType() == AtomicBoolean.class) {
                AtomicBoolean atomicBoolean = getField(AtomicBoolean.class, field);
                if (atomicBoolean == null) {
                    setField(field, new AtomicBoolean(obj.getBoolean(key, (boolean) Util.getOrDefault(getDefaultValue(field), false))));
                } else {
                    atomicBoolean.set(obj.getBoolean(key, (boolean) Util.getOrDefault(getDefaultValue(field), false)));
                }
            } else if (field.getType() == int.class || field.getType() == AtomicInteger.class) {
                ParamInteger param = field.getAnnotation(ParamInteger.class);
                int min = Integer.MIN_VALUE;
                int max = Integer.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                int value = Util.clamp(obj.getInt(key, (int) Util.getOrDefault(getDefaultValue(field), 0)), min, max);
                if (field.getType() == int.class) setField(field, value);
                if (field.getType() == AtomicInteger.class) {
                    AtomicInteger atomicInteger = getField(AtomicInteger.class, field);
                    if (atomicInteger == null) {
                        setField(field, new AtomicInteger(value));
                    } else {
                        atomicInteger.set(value);
                    }
                }
            } else if (field.getType() == long.class || field.getType() == AtomicLong.class) {
                ParamLong param = field.getAnnotation(ParamLong.class);
                long min = Long.MIN_VALUE;
                long max = Long.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                long value = Util.clamp(obj.getLong(key, (long) Util.getOrDefault(getDefaultValue(field), 0)), min, max);
                if (field.getType() == long.class) setField(field, value);
                if (field.getType() == AtomicLong.class) {
                    AtomicLong atomicLong = getField(AtomicLong.class, field);
                    if (atomicLong == null) {
                        setField(field, new AtomicLong(value));
                    } else {
                        atomicLong.set(value);
                    }
                }
            } else if (field.getType() == double.class) {
                ParamDouble param = field.getAnnotation(ParamDouble.class);
                double min = Double.MIN_VALUE;
                double max = Double.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                double value = Util.clamp(obj.getDouble(key, (double) Util.getOrDefault(getDefaultValue(field), 0.0)), min, max);
                setField(field, value);
            } else if (field.getType() == float.class) {
                ParamFloat param = field.getAnnotation(ParamFloat.class);
                float min = Float.MIN_VALUE;
                float max = Float.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                float value = Util.clamp(obj.getFloat(key, (float) Util.getOrDefault(getDefaultValue(field), 0.0f)), min, max);
                setField(field, value);
            } else if (field.getType() == byte.class) {
                ParamByte param = field.getAnnotation(ParamByte.class);
                byte min = Byte.MIN_VALUE;
                byte max = Byte.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                byte value = Util.clamp(obj.getByte(key, (byte) Util.getOrDefault(getDefaultValue(field), 0)), min, max);
                setField(field, value);
            } else if (field.getType() == short.class) {
                ParamShort param = field.getAnnotation(ParamShort.class);
                short min = Short.MIN_VALUE;
                short max = Short.MAX_VALUE;
                if (param != null) {
                    min = param.min();
                    max = param.max();
                }
                short value = Util.clamp(obj.getShort(key, (short) Util.getOrDefault(getDefaultValue(field), 0)), min, max);
                setField(field, value);
            } else if (field.getType() == String.class) {
                setField(field, obj.getString(key));
            } else if (field.getType() == Class.class) {
                try {
                    setField(field, Class.forName(obj.getString(key)));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isEnum()) {
                Object o = obj.getRawData().get(key);
                if (o instanceof Enum<?>) {
                    setField(field, o);
                } else if (o instanceof String s) {
                    setField(field, Enum.valueOf(field.getType().asSubclass(Enum.class), s));
                }
            }
        }
        for (Class<?> c : clazz.getClasses()) {
            if (!c.isAnnotationPresent(Config.class)) continue;
            if (shouldSkip(c)) continue;
            String key = getKey(c);
            if (key == null) continue;
            YamlObject object = obj.getObject(key);
            if (object != null) load(object, c);
        }
    }

    private static boolean shouldReverse(AnnotatedElement element) {
        return element.isAnnotationPresent(Reverse.class);
    }

    private static boolean requiresMCRestart(AnnotatedElement element) {
        return element.isAnnotationPresent(RequiresMCRestart.class);
    }

    @Nullable
    private static Component getDescription(AnnotatedElement element) {
        Description description = element.getAnnotation(Description.class);
        if (description == null) return null;
        BaseComponent baseComponent = null;
        for (Name name : description.value()) {
            if (baseComponent != null) {
                baseComponent.append("\n").append(toComponent(element, name));
            } else {
                baseComponent = toComponent(element, name);
            }
        }
        return baseComponent;
    }

    private static BaseComponent toComponent(AnnotatedElement element, Name name) {
        if (name.path().length() == 0) return new TextComponent(name.value());
        String namespace = name.namespace();
        if (namespace.length() == 0) {
            BlueberryMod mod = null;
            if (element instanceof Field field) {
                mod = BlueberryMod.detectModFromClass(field.getDeclaringClass());
            } else if (element instanceof Method method) {
                mod = BlueberryMod.detectModFromClass(method.getDeclaringClass());
            } else if (element instanceof Class<?> clazz) {
                mod = BlueberryMod.detectModFromClass(clazz);
            }
            if (mod != null) {
                namespace = mod.getModId();
            } else {
                namespace = "minecraft";
            }
        }
        return new BlueberryText(namespace, name.path());
    }

    private static boolean shouldSkip(AnnotatedElement element) {
        HideOn hideOn = element.getAnnotation(HideOn.class);
        if (hideOn == null) return false;
        for (Side side : hideOn.value()) {
            if (side == Side.CLIENT && Blueberry.isClient()) return true;
            if (side == Side.SERVER && Blueberry.isServer()) return true;
            if (side == Side.BOTH) return true;
        }
        return false;
    }

    @Nullable
    private static String getKey(AnnotatedElement element) {
        Key key = element.getAnnotation(Key.class);
        if (key == null) return null;
        return key.value();
    }

    private static int getOrder(AnnotatedElement element) {
        Order order = element.getAnnotation(Order.class);
        if (order == null) return 0;
        return order.value();
    }

    private static void setField(Field field, Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Failed to set config at '{}'", field.toGenericString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(@SuppressWarnings("unused") Class<T> clazz, Field field) {
        try {
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Failed to get (initial) config value at '{}'", field.toGenericString(), e);
            return null;
        }
    }

    private static Object getDefaultValue(Field field) {
        Object o = _getDefaultValue(field);
        if (o instanceof AtomicBoolean atomicBoolean) return atomicBoolean.get();
        if (o instanceof AtomicInteger atomicInteger) return atomicInteger.get();
        if (o instanceof AtomicLong atomicLong) return atomicLong.get();
        return o;
    }

    @Nullable
    private static Object _getDefaultValue(Field field) {
        try {
            Field f = field.getDeclaringClass().getField("defaultValue_" + field.getName());
            return f.get(null);
        } catch (ReflectiveOperationException ignore) {}
        DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
        if (defaultValue == null) return null;
        Class<?> type = field.getType();
        if (defaultValue.s().length() > 0 && (type == String.class || type == Class.class || type.isEnum())) {
            if (type.isEnum()) {
                //try {
                    return Enum.valueOf(type.asSubclass(Enum.class), defaultValue.s());
                //} catch (IllegalArgumentException ignore) { return null; }
            }
            if (type == Class.class) {
                try {
                    return Class.forName(defaultValue.s());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                    //return null;
                }
            }
            if (type == String.class) {
                return defaultValue.s();
            }
        }
        if (type.isEnum()) {
            //try {
                return type.getEnumConstants()[defaultValue.i()];
            //} catch (IndexOutOfBoundsException ignore) { return null; }
        }
        if (type == AtomicInteger.class || type == byte.class || type == short.class || type == int.class) {
            return defaultValue.i();
        }
        if (type == long.class || type == AtomicLong.class) {
            return defaultValue.l();
        }
        if (type == double.class) {
            return defaultValue.d();
        }
        if (type == float.class) {
            return defaultValue.f();
        }
        if (type == AtomicBoolean.class || type == boolean.class) {
            return defaultValue.b();
        }
        return null;
    }

    // annotations that can be applied to methods
    /**
     * When a method is annotated with @SaveHandler, the method would be invoked every config changes.
     * <p>Requirements:
     * <ul>
     *     <li>Return type of void</li>
     *     <li>No parameters</li>
     *     <li>Public (no package-private, protected, or private)</li>
     *     <li>Static</li>
     * </ul>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SaveHandler {}

    // annotations that can be applied to classes
    /**
     * Marks the class as config class. Required on config classes (including root and inner class).
     * The config class must be public (and its fields too), but may be abstract.
     * @see #createFromClass(BlueberryMod, Component, Class) 
     * @see #createFromClass(Class) 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Config {}

    // annotations that can be applied to config field (you MUST annotate with @Name)
    /**
     * Specifies the title of config class or friendly name of field. If <code>path</code> (and/or
     * <code>namespace</code>) was provided, {@link BlueberryText} will be used for the text. If <code>path</code> was
     * not provided but <code>value</code> was provided, {@link TextComponent} will be used for the text. If
     * <code>value</code> and <code>path</code> was empty,
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Name {
        String value() default "";

        String namespace() default "";
        String path() default "";
    }

    /**
     * Specifies the description of the config field.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Description {
        Name[] value();
    }

    /**
     * Specifies the default value of field.
     * <ul>
     *     <li>When used on <code>String</code> or <code>Class</code> field, then <code>string()</code> will be used.</li>
     *     <li>When used on <code>byte</code>, <code>short</code>, or <code>int</code> field, then <code>i()</code> will be used.</li>
     *     <li>When used on <code>long</code> field, then <code>l()</code> will be used.</li>
     *     <li>When used on <code>double</code>, then <code>d()</code> will be used.</li>
     *     <li>When used on <code>float</code>, then <code>f()</code> will be used.</li>
     *     <li>When used on <code>boolean</code> field, then <code>b()</code> will be used.</li>
     *     <li>When used on <code>Enum</code> field, then <code>string()</code> (field name) or <code>i()</code> (ordinal) will be used.</li>
     * </ul>
     * Or, you can define the field named <code>defaultValue_<i><b>config_key</b></i></code> (in case @DefaultValue doesn't support).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface DefaultValue {
        String s() default "";
        int i() default 0;
        long l() default 0;
        double d() default 0;
        float f() default 0;
        boolean b() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamInteger {
        int min() default Integer.MIN_VALUE;
        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamLong {
        long min();
        long max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamDouble {
        double min();
        double max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamFloat {
        float min();
        float max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamByte {
        byte min();
        byte max();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamShort {
        short min();
        short max();
    }

    /**
     * Configures the display order. Negative order would display at first, and positive order would display at last.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Order {
        int value();
    }

    /**
     * Configures the config key which can be used to save/load configurations. Provided key will be set as id when
     * invoking onChanged/save handler. Required for all fields and classes you wish to save, but not needed for root.
     * (if the root class is annotated with @Key, it does nothing.)
     * For example, see {@link net.blueberrymc.common.bml.InternalBlueberryModConfig}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface Key {
        String value();
    }

    /**
     * When the class or field is annotated with @HideOn(...), the annotated configuration would be hidden from the
     * specific side. If annotated on class, all configurations defined under a class would be hidden from the side.
     * (This annotation just tells the VisualConfigManager not to add visual config)
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    public @interface HideOn {
        Side[] value();
    }

    /**
     * Adds "Requires MC restart" in description when annotated with this.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RequiresMCRestart {}

    /**
     * Marks the CycleVisualConfig to act like a reversed list. No effect on other config types.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Reverse {}
}
