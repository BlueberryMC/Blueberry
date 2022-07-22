package net.blueberrymc.gradle.buildSrc.constants

const val API_VERSION = "1.5.0-SNAPSHOT"
const val TTS_VERSION = "1.13.9"
const val LOG4J_VERSION = "2.17.1"
const val LWJGL_VERSION = "3.3.1"
@Suppress("unused") // see .github/workflows/build.yml
const val MAJOR_VERSION = "1.19"
const val MINECRAFT_VERSION = "1.19.1-rc2"
const val CLIENT_JAR_URL = "https://piston-data.mojang.com/v1/objects/f8291ce57a856c0e4ca6f02af3eded3148d3e70c/client.jar"
const val SERVER_JAR_URL = "https://piston-data.mojang.com/v1/objects/5ec09b2700e5e83380a23cb18e66cfdaa931640b/server.jar"
const val CLIENT_MAPPING_URL = "https://piston-data.mojang.com/v1/objects/2294071d96e59cf77fe8193576e97a8133e2c47b/client.txt"
const val MAPPING_VERSION = "22w05a"
const val KOTLIN_VERSION = "1.7.0"

val SERVER_REPOSITORIES_LIST = listOf(
    "https://repo.blueberrymc.net/repository/maven-public/",
    "https://repo.spongepowered.org/maven/",
    "https://jitpack.io",
    "https://libraries.minecraft.net/",
)
val SERVER_LIBRARIES_LIST = listOf(
    "com.google.guava:guava:31.0.1-jre",
    "it.unimi.dsi:fastutil:8.5.6",
    "io.netty:netty-all:4.1.77.Final",
    "com.mojang:brigadier:1.0.18",
    "com.mojang:datafixerupper:5.0.28",
    "com.mojang:javabridge:1.2.24",
    "com.mojang:authlib:3.11.49",
    "com.mojang:logging:1.0.0",
    "net.java.dev.jna:jna:5.9.0",
    "net.java.dev.jna:jna-platform:5.9.0",
    "net.sf.jopt-simple:jopt-simple:5.0.4",
    "com.github.oshi:oshi-core:5.8.2",
    "com.mojang:blocklist:1.0.10", // we probably don't need this
    "net.java.jutils:jutils:1.0.0",
    "com.ibm.icu:icu4j:69.1",
    "org.apache.commons:commons-lang3:3.12.0",
    "commons-io:commons-io:2.11.0",
    "commons-logging:commons-logging:1.2",
    "org.apache.commons:commons-compress:1.21",
    "org.apache.httpcomponents:httpclient:4.5.13",
    "org.apache.httpcomponents:httpcore:4.4.14",
    "org.apache.logging.log4j:log4j-api:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-core:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-slf4j18-impl:$LOG4J_VERSION",
    "org.slf4j:slf4j-api:1.8.0-beta4",
    "ca.weblite:java-objc-bridge:1.1",
    "org.yaml:snakeyaml:1.29",
    "org.ow2.asm:asm:9.2",
    "org.ow2.asm:asm-tree:9.2",
    "org.ow2.asm:asm-analysis:9.2",
    "org.ow2.asm:asm-commons:9.2",
    "org.ow2.asm:asm-util:9.2",
    "org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION",
    "org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION",
)
val SERVER_LIBRARIES_EXCLUDES_LIST = listOf(
    "^asm:asm:.*$",
)
