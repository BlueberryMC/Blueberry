package net.blueberrymc.gradle.buildSrc

import net.blueberrymc.gradle.buildSrc.util.ArrayUtil
import net.blueberrymc.gradle.buildSrc.util.StreamUtil.setupPrinter
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.transport.URIish
import java.io.File
import java.util.Objects

object Util {
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
            git.checkout().setCreateBranch(true).setStartPoint("upstream/master").setName("master").call()
        }
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("upstream/master").call()
        val patchesDir = File(baseDir, patchesDirName)
        // requires git to be installed, but who cares? you would need git to work on patches anyway :P
        ProcessBuilder("git", "am", "--abort").directory(repositoryDir).start().waitFor()
        val res = ProcessBuilder(*ArrayUtil.concat(
            arrayOf("git", "am", "--ignore-whitespace", "--3way"), getFilesInDirectory(patchesDir).map { it.absolutePath }.toTypedArray()
        )).directory(repositoryDir).inheritIO().start().setupPrinter().waitFor()
        if (res != 0) {
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
}
