package net.blueberrymc.gradle.buildSrc.constants

import org.eclipse.jgit.internal.storage.file.FileRepository
import java.io.File
import java.io.IOException

object Repositories {
    fun getBlueberryRoot(project: File) = getRepository(project)
    fun getBlueberryClient(project: File) = getRepository(File(project, "Blueberry-Client"))

    fun getRepository(file: File): FileRepository {
        // check if exists and throw IOException
        if (!file.exists()) {
            throw IOException("Repository not found")
        }
        // check if file is directory
        if (!file.isDirectory) {
            throw IOException("Repository is not a directory")
        }
        return FileRepository(file)
    }
}
