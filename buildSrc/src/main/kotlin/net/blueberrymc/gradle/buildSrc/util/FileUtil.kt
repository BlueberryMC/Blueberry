package net.blueberrymc.gradle.buildSrc.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileUtil {
    fun unzip(zipFile: String, outputDirectory: String) {
        val buffer = ByteArray(1024 * 32) // 32kb
        ZipInputStream(FileInputStream(zipFile)).use { zip ->
            var zipEntry: ZipEntry? = zip.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(outputDirectory + File.separator + fileName)
                newFile.parentFile.mkdirs()
                FileOutputStream(newFile).use { fos ->
                    var len: Int
                    while (zip.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
                zipEntry = zip.nextEntry
            }
        }
    }
}
