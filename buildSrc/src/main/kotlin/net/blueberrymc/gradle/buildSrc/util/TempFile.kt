package net.blueberrymc.gradle.buildSrc.util

import java.nio.file.Path

data class TempFile(val path: Path): AutoCloseable {
    override fun close() {
        path.toFile().delete()
    }
}
