package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumberUtil {
    @Contract("null, _ -> false; !null, null -> false")
    public static boolean isNumberLessThan(@Nullable Number number, @Nullable Number another) {
        if (number == null || another == null) return false;
        return isByteLessThan(number, another)
                || isDoubleLessThan(number, another)
                || isFloatLessThan(number, another)
                || isIntegerLessThan(number, another)
                || isLongLessThan(number, another)
                || isShortLessThan(number, another);
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isByteLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Byte n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isDoubleLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Double n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isFloatLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Float n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isIntegerLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Integer n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isLongLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Long n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isShortLessThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Short n) {
            if (another instanceof Byte b) {
                return n < b;
            } else if (another instanceof Double d) {
                return n < d;
            } else if (another instanceof Float f) {
                return n < f;
            } else if (another instanceof Integer i) {
                return n < i;
            } else if (another instanceof Long l) {
                return n < l;
            } else if (another instanceof Short s) {
                return n < s;
            }
        }
        return false;
    }

    @Contract("null, _ -> false; !null, null -> false")
    public static boolean isNumberGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number == null || another == null) return false;
        return isByteGreaterThan(number, another)
                || isDoubleGreaterThan(number, another)
                || isFloatGreaterThan(number, another)
                || isIntegerGreaterThan(number, another)
                || isLongGreaterThan(number, another)
                || isShortGreaterThan(number, another);
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isByteGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Byte n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isDoubleGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Double n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isFloatGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Float n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isIntegerGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Integer n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isLongGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Long n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    @Contract(value = "null, _ -> false; _, null -> false", pure = true)
    public static boolean isShortGreaterThan(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Short n) {
            if (another instanceof Byte b) {
                return n > b;
            } else if (another instanceof Double d) {
                return n > d;
            } else if (another instanceof Float f) {
                return n > f;
            } else if (another instanceof Integer i) {
                return n > i;
            } else if (another instanceof Long l) {
                return n > l;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }

    public static @NotNull Number multiply(@Nullable Number number, @Nullable Number another) {
        if (number == null || another == null) return 0;
        if (number instanceof Byte) {
            return multiplyByte(number, another);
        }
        if (number instanceof Double) {
            return multiplyDouble(number, another);
        }
        if (number instanceof Float) {
            return multiplyFloat(number, another);
        }
        if (number instanceof Integer) {
            return multiplyInteger(number, another);
        }
        if (number instanceof Long) {
            return multiplyLong(number, another);
        }
        if (number instanceof Short) {
            return multiplyShort(number, another);
        }
        throw new RuntimeException("Unknown number type: " + number.getClass().getTypeName());
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static byte multiplyByte(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Byte n) {
            if (another instanceof Byte b) {
                return (byte) (n * b);
            } else if (another instanceof Double d) {
                return (byte) (n * d);
            } else if (another instanceof Float f) {
                return (byte) (n * f);
            } else if (another instanceof Integer i) {
                return (byte) (n * i);
            } else if (another instanceof Long l) {
                return (byte) (n * l);
            } else if (another instanceof Short s) {
                return (byte) (n * s);
            }
        }
        throw new ClassCastException("number is not a byte");
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static double multiplyDouble(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Double n) {
            if (another instanceof Byte b) {
                return n * b;
            } else if (another instanceof Double d) {
                return n * d;
            } else if (another instanceof Float f) {
                return n * f;
            } else if (another instanceof Integer i) {
                return n * i;
            } else if (another instanceof Long l) {
                return n * l;
            } else if (another instanceof Short s) {
                return n * s;
            }
        }
        throw new ClassCastException("number is not a double");
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static float multiplyFloat(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Float n) {
            if (another instanceof Byte b) {
                return n * b;
            } else if (another instanceof Double d) {
                return (float) (n * d);
            } else if (another instanceof Float f) {
                return n * f;
            } else if (another instanceof Integer i) {
                return n * i;
            } else if (another instanceof Long l) {
                return n * l;
            } else if (another instanceof Short s) {
                return n * s;
            }
        }
        throw new ClassCastException("number is not a float");
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static int multiplyInteger(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Integer n) {
            if (another instanceof Byte b) {
                return n * b;
            } else if (another instanceof Double d) {
                return (int) (n * d);
            } else if (another instanceof Float f) {
                return (int) (n * f);
            } else if (another instanceof Integer i) {
                return n * i;
            } else if (another instanceof Long l) {
                return (int) (n * l);
            } else if (another instanceof Short s) {
                return n * s;
            }
        }
        throw new ClassCastException("number is not a integer");
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static long multiplyLong(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Long n) {
            if (another instanceof Byte b) {
                return n * b;
            } else if (another instanceof Double d) {
                return (long) (n * d);
            } else if (another instanceof Float f) {
                return (long) (n * f);
            } else if (another instanceof Integer i) {
                return n * i;
            } else if (another instanceof Long l) {
                return n * l;
            } else if (another instanceof Short s) {
                return n * s;
            }
        }
        throw new ClassCastException("number is not a long");
    }

    @Contract(value = "null, _ -> fail; _, null -> fail", pure = true)
    public static short multiplyShort(@Nullable Number number, @Nullable Number another) {
        if (number instanceof Short n) {
            if (another instanceof Byte b) {
                return (short) (n * b);
            } else if (another instanceof Double d) {
                return (short) (n * d);
            } else if (another instanceof Float f) {
                return (short) (n * f);
            } else if (another instanceof Integer i) {
                return (short) (n * i);
            } else if (another instanceof Long l) {
                return (short) (n * l);
            } else if (another instanceof Short s) {
                return (short) (n * s);
            }
        }
        throw new ClassCastException("number is not a short");
    }
}
