import net.blueberrymc.gradle.buildSrc.constants.*

version = apiVersion

dependencies {
    compileOnly("net.blueberrymc.magmacube:magmacube:$minecraftVersion")
    compileOnly("org.jetbrains:annotations:23.0.0")
    api("net.arikia.dev:discord-rpc-without-jna:1.7.0")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("com.google.code.gson:gson:2.8.8")
    api("com.google.guava:guava:31.0.1-jre")
    api("com.mojang:brigadier:1.0.18")
    api("com.mojang:datafixerupper:4.0.26")
    api("com.mojang:javabridge:1.2.24")
    api("com.mojang:authlib:3.2.38")
    api("it.unimi.dsi:fastutil:8.5.6")
    api("io.netty:netty-all:4.1.68.Final")
    api("org.apache.commons:commons-lang3:3.12.0")
    api("org.yaml:snakeyaml:1.29")
    api("org.ow2.asm:asm:9.2")
    api("org.ow2.asm:asm-tree:9.2")
    api("org.ow2.asm:asm-analysis:9.2")
    api("org.ow2.asm:asm-commons:9.2")
    api("org.ow2.asm:asm-util:9.2")
    api("org.spongepowered:mixin:0.8.5")
    api("net.blueberrymc:native-util:2.1.0")
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
