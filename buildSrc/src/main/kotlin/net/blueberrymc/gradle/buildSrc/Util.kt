package net.blueberrymc.gradle.buildSrc

import net.blueberrymc.gradle.buildSrc.util.ArrayUtil
import net.blueberrymc.gradle.buildSrc.util.StreamUtil.setupPrinter
import net.blueberrymc.gradle.buildSrc.util.TempFile
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.transport.URIish
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.net.URLClassLoader
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Objects
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.io.path.outputStream

object Util {
    fun downloadFile(url: String, destination: File) {
        URL(url).openStream().use { it.transferTo(destination.outputStream()) }
    }

    fun applyPatches(baseDir: File, targetDirName: String, patchesDirName: String, upstreamPath: File) {
        val repositoryDir = File(baseDir, targetDirName)
        if (!repositoryDir.exists()) {
            repositoryDir.mkdir()
        }
        val git = Git.init().setDirectory(repositoryDir).call()
        git.remoteRemove().setRemoteName("upstream").call()
        git.remoteAdd().setName("upstream").setUri(URIish(upstreamPath.toURI().toURL())).call()
        git.fetch().setRemote("upstream").call()
        try {
            git.checkout().setName("master").call()
        } catch (e: Exception) {
            try {
                git.checkout().setCreateBranch(true).setStartPoint("upstream/master").setName("master").call()
            } catch (e2: Exception) {
                e2.addSuppressed(e)
                throw e2
            }
        }
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("upstream/master").call()
        val patchesDir = File(baseDir, patchesDirName)
        // requires git to be installed, but who cares? you would need git to work on patches anyway :P
        ProcessBuilder("git", "am", "--abort").directory(repositoryDir).start().waitFor()
        val res = ProcessBuilder(*ArrayUtil.concat(
            arrayOf("git", "am", "--ignore-whitespace", "--3way"), getFilesInDirectory(patchesDir).map { it.absolutePath }.toTypedArray()
        )).directory(repositoryDir).inheritIO().start().setupPrinter().waitFor()
        if (res != 0) {
            if (res == 128) {
                println("Process exited with code 128. You probably forgot to set user.email and user.name in your git config.")
            }
            throw RuntimeException("Failed to apply patches (exited with code $res)")
        }
    }

    fun rebuildPatches(baseDir: File, targetDirName: String, patchesDirName: String) {
        val patchesDir = File(baseDir, patchesDirName)
        patchesDir.deleteRecursively()
        patchesDir.mkdir()
        val repositoryDir = File(baseDir, targetDirName)
        if (!repositoryDir.exists()) {
            error("$repositoryDir does not exist")
        }
        val res = ProcessBuilder("git", "format-patch", "--zero-commit", "--full-index", "--no-signature", "--no-stat", "-N", "-o", patchesDir.absolutePath, "upstream/master")
            .directory(repositoryDir).start().waitFor()
        if (res != 0) {
            throw RuntimeException("Failed to rebuild patches (exited with code $res)")
        }
        ProcessBuilder("git", "add", "-A").start().waitFor()
    }

    private fun getFilesInDirectory(dir: File): List<File> =
        Objects.requireNonNull(dir.listFiles()).filter { it.isFile }.sorted().toList()

    fun runMain(classpath: List<File>, mainClass: String, args: Array<String>) {
        val ucl = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())
        try {
            Class.forName(mainClass, true, ucl).getMethod("main", Array<String>::class.java).invoke(null, args)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Class $mainClass not found.\nclasspath:\n  ${classpath.joinToString("\n  ")}", e)
        }
    }

    private var warnedBuildNumber = false

    fun getBuildNumber(project: Project): Long = project.properties["BUILD_NUMBER"].toString().toLongOrNull().let {
        if (it == null) {
            if (!warnedBuildNumber) {
                warnedBuildNumber = true
                project.logger.warn("Invalid BUILD_NUMBER: ${project.properties["BUILD_NUMBER"]}. Using 0 instead.")
            }
            0
        } else {
            it
        }
    }

    fun sha256sum(bytes: ByteArray): String /* 64 characters */ {
        val digest = MessageDigest.getInstance("SHA-256")
        return bytesToHex(digest.digest(bytes)).substring(0, 64)
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    // archive all files into .jar file
    fun jar(archiveFile: File, fileOrDirectory: File) {
        JarOutputStream(FileOutputStream(archiveFile)).use { jar ->
            jar(jar, fileOrDirectory, null)
        }
    }

    fun jar(jar: JarOutputStream, fileOrDirectory: File, prefix: String?) {
        if (fileOrDirectory.isFile) {
            val entry = JarEntry((prefix ?: "") + fileOrDirectory.name)
            entry.size = fileOrDirectory.length()
            jar.putNextEntry(entry)
            FileInputStream(fileOrDirectory).use {
                it.copyTo(jar)
            }
            jar.closeEntry()
        } else {
            val newPrefix = if (prefix == null) {
                ""
            } else {
                "$prefix${fileOrDirectory.name}/"
            }
            for (file in fileOrDirectory.listFiles()!!) {
                jar(jar, file, newPrefix)
            }
        }
    }

    /**
     * Downloads the file from the given URL to temporary file and returns it. If not closed, the file will not be
     * deleted.
     * @param url URL to download
     * @return temporary file
     */
    fun downloadFile(url: String): TempFile {
        val tempFile = TempFile(File.createTempFile("download", ".tmp").toPath())
        val connection = URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "BlueberryMC/Blueberry (buildSrc)")
        connection.connect()
        connection.getInputStream().use { input ->
            tempFile.path.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    fun getMojangDateTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = (calendar[Calendar.MONTH] + 1).toString().padStart(2, '0')
        val day = calendar[Calendar.DAY_OF_MONTH].toString().padStart(2, '0')
        val hour = (calendar[Calendar.HOUR_OF_DAY]).toString().padStart(2, '0')
        val minute = (calendar[Calendar.MINUTE]).toString().padStart(2, '0')
        val second = (calendar[Calendar.SECOND]).toString().padStart(2, '0')
        val tz = ZoneId.of(calendar.timeZone.id).rules.getOffset(Instant.now()).id // example: +09:00
        return "$year-$month-${day}T$hour:$minute:$second$tz"
    }
}
