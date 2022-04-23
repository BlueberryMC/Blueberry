package net.blueberrymc.gradle.buildSrc.constants

const val API_VERSION = "1.4.1-SNAPSHOT"
const val TTS_VERSION = "1.12.4"
const val LOG4J_VERSION = "2.17.1"
const val LWJGL_VERSION = "3.2.2"
const val MINECRAFT_VERSION = "1.18.2"
const val CLIENT_JAR_URL = "https://launcher.mojang.com/v1/objects/2e9a3e3107cca00d6bc9c97bf7d149cae163ef21/client.jar"
const val SERVER_JAR_URL = "https://launcher.mojang.com/v1/objects/c8f83c5655308435b3dcf03c06d9fe8740a77469/server.jar"
const val CLIENT_MAPPING_URL = "https://launcher.mojang.com/v1/objects/a661c6a55a0600bd391bdbbd6827654c05b2109c/client.txt"
const val MAPPING_VERSION = "22w05a"
val SERVER_REPOSITORIES_LIST = listOf(
    "https://repo.blueberrymc.net/repository/maven-public/",
    "https://repo.spongepowered.org/maven/",
    "https://jitpack.io",
    "https://libraries.minecraft.net/",
)
val SERVER_LIBRARIES_LIST = listOf(
    "com.google.guava:guava:31.0.1-jre",
    "org.yaml:snakeyaml:1.29",
    "org.ow2.asm:asm:9.2",
    "org.ow2.asm:asm-tree:9.2",
    "org.ow2.asm:asm-analysis:9.2",
    "org.ow2.asm:asm-commons:9.2",
    "org.ow2.asm:asm-util:9.2",
    "it.unimi.dsi:fastutil:8.5.6",
    "io.netty:netty-all:4.1.68.Final",
    "com.mojang:brigadier:1.0.18",
    "com.mojang:datafixerupper:4.0.26",
    "com.mojang:javabridge:1.2.24",
    "com.mojang:authlib:3.2.38",
    "com.mojang:logging:1.0.0",
    "net.java.dev.jna:jna:5.9.0",
    "net.java.dev.jna:jna-platform:5.9.0",
    "net.sf.jopt-simple:jopt-simple:5.0.4",
    "com.github.oshi:oshi-core:5.8.2",
    "com.mojang:blocklist:1.0.6", // we probably don't need this
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
)
val SERVER_LIBRARIES_EXCLUDES_LIST = listOf(
    "^asm:asm:.*$",
)
