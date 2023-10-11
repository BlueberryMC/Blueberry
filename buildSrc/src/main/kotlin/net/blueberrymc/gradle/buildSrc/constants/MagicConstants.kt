package net.blueberrymc.gradle.buildSrc.constants

const val API_VERSION = "2.0.0-SNAPSHOT"
const val TTS_VERSION = "1.17.9"
const val LOG4J_VERSION = "2.19.0"
const val LWJGL_VERSION = "3.3.2"
@Suppress("unused") // see .github/workflows/build.yml
const val MAJOR_VERSION = "1.20"
const val MINECRAFT_VERSION = "1.20.2"
const val CLIENT_JAR_URL = "https://piston-data.mojang.com/v1/objects/82d1974e75fc984c5ed4b038e764e50958ac61a0/client.jar"
const val SERVER_JAR_URL = "https://piston-data.mojang.com/v1/objects/5b868151bd02b41319f54c8d4061b8cae84e665c/server.jar"
const val CLIENT_MAPPING_URL = "https://piston-data.mojang.com/v1/objects/5c292ff7d3161977041116698e295083fd5ec8f5/client.txt"
const val MAPPING_VERSION = "22w05a"
const val KOTLIN_VERSION = "1.9.10"

val SERVER_REPOSITORIES_LIST = listOf(
    "https://repo.azisaba.net/repository/maven-public/",
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
    "org.ow2.asm:asm:9.4",
    "org.ow2.asm:asm-tree:9.4",
    "org.ow2.asm:asm-analysis:9.4",
    "org.ow2.asm:asm-commons:9.4",
    "org.ow2.asm:asm-util:9.4",
    "org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION",
    "org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION",
    "org.joml:joml:1.10.5",
)
val SERVER_LIBRARIES_EXCLUDES_LIST = listOf(
    "^asm:asm:.*$",
)
