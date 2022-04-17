package net.blueberrymc.gradle.buildSrc.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
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

    fun deleteRecursively(file: File, depth: Int = 0) {
        if (file.isFile) {
            file.delete()
        } else {
            val tasks = mutableListOf<ForkJoinTask<*>>()
            file.listFiles()?.forEach {
                if (depth % 2 == 0) {
                    tasks.add(ForkJoinPool.commonPool().submit {
                        deleteRecursively(it, depth + 1)
                    })
                } else {
                    deleteRecursively(it, depth + 1)
                }
            }
            tasks.forEach { it.join() }
            file.delete()
        }
    }

    fun File.forEachFile(action: (File) -> Unit) {
        action(this)
        if (isDirectory) {
            listFiles()?.forEach {
                it.forEachFile(action)
            }
        }
    }
}
