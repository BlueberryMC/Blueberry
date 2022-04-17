package net.blueberrymc.gradle.buildSrc.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileUtil {
    fun unzip(zipFile: String, outputDirectory: String) {
        ZipInputStream(FileInputStream(zipFile)).use { zip ->
            var zipEntry: ZipEntry? = zip.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(outputDirectory + File.separator + fileName)
                //println("Extracting: $newFile${if (zipEntry.isDirectory) "/" else ""}")
                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile.mkdirs()
                    FileOutputStream(newFile).use { zip.transferTo(it) }
                }
                zipEntry = zip.nextEntry
            }
        }
    }
}
