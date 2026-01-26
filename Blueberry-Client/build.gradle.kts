import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION

plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.1"
}

version = MINECRAFT_VERSION
group = "net.blueberrymc"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
}

dependencies {
    api(project(":blueberry-api"))
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.officialMojangMappings())
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    compileOnlyApi("net.fabricmc:fabric-loader:0.18.4")
    runtimeOnly(loom.namedMinecraftJars)
    runtimeOnly("org.ow2.asm:asm:9.7")
    runtimeOnly("org.ow2.asm:asm-tree:9.7")
    runtimeOnly("org.ow2.asm:asm-analysis:9.7")
    runtimeOnly("org.ow2.asm:asm-commons:9.7")
    runtimeOnly("org.ow2.asm:asm-util:9.7")
    //mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")
    configurations.create("realShadow") {
        isCanBeResolved = true
        isCanBeConsumed = true
        this(project(":blueberry-api"))
        this(loom.namedMinecraftJars)
    }
}

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

    mixin {
        useLegacyMixinAp.set(true)
        add(sourceSets.main.get(), "blueberry.mixins.refmap.json")
    }
}

tasks {
    runClient {
        mainClass.set("net.blueberrymc.client.main.ClientMain")
        jvmArgs("-Dmixin.debug=true")
        args("--accessToken=foobar", "--version=$MINECRAFT_VERSION")
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
