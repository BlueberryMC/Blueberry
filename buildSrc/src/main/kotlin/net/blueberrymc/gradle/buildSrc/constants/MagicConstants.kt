package net.blueberrymc.gradle.buildSrc.constants

const val API_VERSION = "1.8.0-SNAPSHOT"
const val TTS_VERSION = "1.13.9"
const val LOG4J_VERSION = "2.19.0"
const val LWJGL_VERSION = "3.3.1"
@Suppress("unused") // see .github/workflows/build.yml
const val MAJOR_VERSION = "1.19"
const val MINECRAFT_VERSION = "1.19.4"
const val CLIENT_JAR_URL = "https://piston-data.mojang.com/v1/objects/958928a560c9167687bea0cefeb7375da1e552a8/client.jar"
const val SERVER_JAR_URL = "https://piston-data.mojang.com/v1/objects/8f3112a1049751cc472ec13e397eade5336ca7ae/server.jar"
const val CLIENT_MAPPING_URL = "https://piston-data.mojang.com/v1/objects/f14771b764f943c154d3a6fcb47694477e328148/client.txt"
const val MAPPING_VERSION = "22w05a"
const val KOTLIN_VERSION = "1.7.21"

val SERVER_REPOSITORIES_LIST = listOf(
    "https://repo.blueberrymc.net/repository/maven-public/",
    "https://repo.spongepowered.org/maven/",
    "https://jitpack.io",
    "https://libraries.minecraft.net/",
)
val SERVER_LIBRARIES_LIST = listOf(
    "com.google.guava:guava:31.1-jre",
    "com.google.code.gson:gson:2.10",
    "it.unimi.dsi:fastutil:8.5.9",
    "io.netty:netty-all:4.1.82.Final",
    "com.mojang:brigadier:1.0.18",
    "com.mojang:datafixerupper:6.0.6",
    "com.mojang:javabridge:2.0.25",
    "com.mojang:authlib:3.18.38",
    "com.mojang:logging:1.1.1",
    "net.java.dev.jna:jna:5.9.0",
    "net.java.dev.jna:jna-platform:5.9.0",
    "net.sf.jopt-simple:jopt-simple:5.0.4",
    "com.github.oshi:oshi-core:6.2.2",
    "com.mojang:blocklist:1.0.10", // we probably don't need this
    "net.java.jutils:jutils:1.0.0",
    "com.ibm.icu:icu4j:71.1",
    "org.apache.commons:commons-lang3:3.12.0",
    "commons-io:commons-io:2.11.0",
    "commons-logging:commons-logging:1.2",
    "org.apache.commons:commons-compress:1.21",
    "org.apache.httpcomponents:httpclient:4.5.13",
    "org.apache.httpcomponents:httpcore:4.4.15",
    "org.apache.logging.log4j:log4j-api:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-core:$LOG4J_VERSION",
    "org.apache.logging.log4j:log4j-slf4j2-impl:$LOG4J_VERSION",
    "org.slf4j:slf4j-api:2.0.1",
    "ca.weblite:java-objc-bridge:1.1",
    "org.yaml:snakeyaml:1.33",
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
