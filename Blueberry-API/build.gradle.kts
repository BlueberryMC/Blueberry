import net.blueberrymc.gradle.buildSrc.constants.*

version = API_VERSION

val adventureVersion = "4.11.0"

dependencies {
    //compileOnly("net.blueberrymc.magmacube:magmacube:$MINECRAFT_VERSION")
    implementation("org.jetbrains:annotations:23.1.0")
    compileOnlyApi("org.jetbrains:annotations:23.1.0")
    compileOnlyApi("com.mojang:brigadier:1.0.18")
    compileOnlyApi("org.lwjgl:lwjgl:3.2.1")
    compileOnlyApi("org.lwjgl:lwjgl-glfw:3.2.1")
    api("net.kyori:adventure-api:$adventureVersion")
    api("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    api("commons-io:commons-io:2.6")
    api("com.github.JnCrMx:discord-game-sdk4j:v0.5.5")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("com.google.code.gson:gson:2.10")
    api("com.google.guava:guava:31.1-jre")
    api("com.mojang:brigadier:1.0.18")
    api("com.mojang:datafixerupper:5.0.28")
    api("com.mojang:javabridge:2.0.25")
    api("com.mojang:authlib:3.16.29")
    api("it.unimi.dsi:fastutil:8.5.9")
    api("io.netty:netty-all:4.1.82.Final")
    api("org.apache.commons:commons-lang3:3.12.0")
    api("org.yaml:snakeyaml:1.33")
    api("org.ow2.asm:asm:9.4")
    api("org.ow2.asm:asm-tree:9.4")
    api("org.ow2.asm:asm-analysis:9.4")
    api("org.ow2.asm:asm-commons:9.4")
    api("org.ow2.asm:asm-util:9.4")
    api("org.spongepowered:mixin:0.8.5")
    api("net.blueberrymc:native-util:2.1.0")
    api("ca.weblite:java-objc-bridge:1.1")
    api("org.joml:joml:1.10.5")
    api("net.minecraft:launchwrapper:1.12") {
        exclude("org.lwjgl.lwjgl", "lwjgl")
        exclude("org.lwjgl", "lwjgl")
        exclude("org.lwjgl", "lwjgl-openal")
        exclude("org.ow2.asm", "asm-debug-all")
    }
}

publishing {
    repositories {
        maven {
            name = "blueberryRepo"
            credentials(PasswordCredentials::class)
            url = uri(
                if (project.version.toString().endsWith("-SNAPSHOT"))
                    "https://repo.blueberrymc.net/repository/maven-snapshots/"
                else
                    "https://repo.blueberrymc.net/repository/maven-releases/"
            )
        }
    }
}

tasks {
    processResources {
        filesMatching("**/*.json") {
            filter(org.apache.tools.ant.filters.EscapeUnicode::class.java)
        }
    }
}
