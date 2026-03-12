import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.2"
}

version = MINECRAFT_VERSION
group = "net.blueberrymc"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

repositories {
}

dependencies {
    api(project(":blueberry-api"))
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    //annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    compileOnlyApi("net.fabricmc:fabric-loader:0.18.4")
//    compileOnly(files(provider {
//        (loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.OFFICIAL)
//    }))
    runtimeOnly("org.ow2.asm:asm:9.9.1")
    runtimeOnly("org.ow2.asm:asm-tree:9.9.1")
    runtimeOnly("org.ow2.asm:asm-analysis:9.9.1")
    runtimeOnly("org.ow2.asm:asm-commons:9.9.1")
    runtimeOnly("org.ow2.asm:asm-util:9.9.1")
    configurations.create("realShadow") {
        isCanBeResolved = true
        isCanBeConsumed = true
        this(project(":blueberry-api"))
//        this(files(provider {
//            (loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.OFFICIAL)
//        }))
    }
}

//afterEvaluate {
//    dependencies {
//        runtimeOnly(loom.namedMinecraftJars)
//    }
//}

java {
    withSourcesJar()
}

loom {
    accessWidenerPath = file("../Blueberry-API/src/main/resources/blueberry.accesswidener")

    mods {
        create("blueberry") {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks {
    runClient {
        mainClass.set("net.blueberrymc.client.main.ClientMain")
        jvmArgs("-Dmixin.debug=true")
        args("--accessToken=foobar", "--version=$MINECRAFT_VERSION")
        environment("GLFW_PLATFORM", "x11")
        environment.remove("WAYLAND_DISPLAY")
        environment("XDG_SESSION_TYPE", "x11")
        //environment("WAYLAND_DISPLAY", "")
        environment("__EGL_VENDOR_LIBRARY_FILENAMES", "/usr/share/glvnd/egl_vendor.d/10_nvidia.json")
    }

    shadowJar {
        configurations.set(listOf(project.configurations.getByName("realShadow")))
        manifest {
            attributes(mapOf(
                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "MixinConfigs" to "blueberry.mixins.json"
            ))
        }
    }
}
