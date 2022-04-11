package net.blueberrymc.gradle.buildSrc.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


object StreamUtil {
    fun setupPrinter(input: InputStream) {
        Thread {
            InputStreamReader(input).use { isr ->
                BufferedReader(isr).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        println(line)
                    }
                }
            }
        }.apply { isDaemon = true }.start()
    }
}
