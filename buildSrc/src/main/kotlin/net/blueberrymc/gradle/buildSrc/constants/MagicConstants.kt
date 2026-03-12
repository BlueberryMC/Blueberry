package net.blueberrymc.gradle.buildSrc.constants

const val API_VERSION = "2.0.0-SNAPSHOT"
const val TTS_VERSION = "1.17.9"
const val LOG4J_VERSION = "2.19.0"
const val LWJGL_VERSION = "3.3.3"
@Suppress("unused") // see .github/workflows/build.yml
const val MAJOR_VERSION = "26"
const val MINECRAFT_VERSION = "26.1-pre-1"
const val CLIENT_JAR_URL = "https://piston-data.mojang.com/v1/objects/a623b4ac667739eb36956494a3e7ce98ff4da095/client.jar"
const val SERVER_JAR_URL = "https://piston-data.mojang.com/v1/objects/07127fc27cad5f29b326cb9c5ef8bb24599e2c11/server.jar"
const val KOTLIN_VERSION = "2.3.10"

val SERVER_REPOSITORIES_LIST = listOf(
    "https://repo.spongepowered.org/maven/",
    "https://jitpack.io",
    "https://libraries.minecraft.net/",
)
val SERVER_LIBRARIES_LIST = listOf(
    "com.google.guava:guava:31.1-jre",
    "com.google.code.gson:gson:2.10",
    "it.unimi.dsi:fastutil:8.5.12",
    "io.netty:netty-all:4.1.97.Final",
    "com.mojang:brigadier:1.1.8",
    "com.mojang:datafixerupper:6.0.8",
    "com.mojang:authlib:5.0.47",
    "com.mojang:logging:1.1.1",
    "net.java.dev.jna:jna:5.9.0",
    "net.java.dev.jna:jna-platform:5.9.0",
    "net.sf.jopt-simple:jopt-simple:5.0.4",
    "com.github.oshi:oshi-core:6.4.5",
    "com.mojang:blocklist:1.0.10",
    "net.java.jutils:jutils:1.0.0",
    "com.ibm.icu:icu4j:73.2",
    "org.apache.commons:commons-lang3:3.13.0",
    "commons-io:commons-io:2.13.0",
    "commons-logging:commons-logging:1.2",
    "org.apache.commons:commons-compress:1.22",
    "org.apache.httpcomponents:httpclient:4.5.14",
    "org.apache.httpcomponents:httpcore:4.4.16",
    "org.apache.logging.log4j:log4j-api:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-core:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-slf4j2-impl:$LOG4J_VERSION",
    "org.slf4j:slf4j-api:2.0.1",
    "ca.weblite:java-objc-bridge:1.1",
    "org.yaml:snakeyaml:2.2",
    "org.ow2.asm:asm:9.7",
    "org.ow2.asm:asm-tree:9.7",
    "org.ow2.asm:asm-analysis:9.7",
    "org.ow2.asm:asm-commons:9.7",
    "org.ow2.asm:asm-util:9.7",
    "org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION",
    "org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION",
    "org.joml:joml:1.10.5",
)
val SERVER_LIBRARIES_EXCLUDES_LIST = listOf(
    "^asm:asm:.*$",
)
