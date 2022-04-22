package net.blueberrymc.gradle.buildSrc.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
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

    fun deleteRecursively(file: File, depth: Int = 0, deleteRoot: Boolean = false) {
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
            if (deleteRoot) {
                file.delete()
            }
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

    /**
     * Copy the files/directories inside `from` to the `to`.
     */
    fun copy(from: Path, to: Path) {
        Files.walk(from).use {
            it.forEach { path ->
                val target = to.resolve(from.relativize(path).toString())
                if (Files.isDirectory(path)) {
                    // if dir
                    Files.createDirectories(target)
                } else {
                    // if file
                    try {
                        Files.copy(path, target)
                    } catch (ignored: IOException) {
                    }
                }
            }
        }
    }

    fun shade(jar: Path, to: Path) {
        val fs = FileSystems.newFileSystem(jar)
        fs.rootDirectories.forEach {
            copy(it, to)
        }
    }
}
