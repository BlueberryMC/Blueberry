import java.util.zip.ZipInputStream

plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    `maven-publish`
}

version = project.properties["minecraft_version"] as String
group = "net.blueberrymc"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings(loom.officialMojangMappings())
    //mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")
}

java {
    withSourcesJar()
}

tasks {
    register("unzipSourcesJar") {
        doLast {
            configurations.runtimeClasspath.get().resolve().forEach { dependency ->
                val path = dependency.absolutePath
                if (path.contains("minecraftMaven") && path.contains("fabric-loom") && path.contains("minecraft-merged")) {
                    val sourcesJar =
                        dependency.parentFile.listFiles()!!.filter { it.name.endsWith("-sources.jar") }.getOrNull(0)
                    if (sourcesJar == null) {
                        error("sources jar does not exist. Run genSources task first!")
                    }
                    val sources = File(project.rootDir, "src/main/java")
                    val resources = File(project.rootDir, "src/main/resources")
                    ZipInputStream(file(sourcesJar).inputStream()).use { stream ->
                        generateSequence { stream.nextEntry }
                            .forEach { entry ->
                                val file = if (entry.name.startsWith("META-INF")) {
                                    File(resources, entry.name)
                                } else {
                                    File(sources, entry.name)
                                }
                                if (entry.isDirectory) {
                                    file.mkdirs()
                                } else {
                                    file.parentFile.mkdirs()
                                    file.writeBytes(stream.readAllBytes())
                                }
                            }
                    }
                }
            }
        }
    }
}
