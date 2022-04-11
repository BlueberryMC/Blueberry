package net.blueberrymc.gradle.buildSrc.util

object ArrayUtil {
    inline fun <reified T> concat(vararg arrays: Array<T>): Array<T> {
        val result = arrayOfNulls<T>(arrays.sumOf { it.size })
        var offset = 0
        for (array in arrays) {
            System.arraycopy(array, 0, result, offset, array.size)
            offset += array.size
        }
        @Suppress("UNCHECKED_CAST")
        return result as Array<T>
    }
}
