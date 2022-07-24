package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class NumberUtil {
    @Contract("null -> false")
    public static boolean isLong(@Nullable String s) {
        if (s == null) {
            return false;
        }
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n < l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
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
            } else if (another instanceof Long l2) {
                return n > l2;
            } else if (another instanceof Short s) {
                return n > s;
            }
        }
        return false;
    }
}
