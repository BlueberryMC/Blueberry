plugins {
    java
    kotlin("jvm") version "1.6.0"
    `maven-publish`
    `java-library`
}

apply<net.blueberrymc.gradle.buildSrc.BuildPlugin>()

group = "net.blueberrymc"
version = "dev-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    group = parent!!.group

    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("maven-publish")
        plugin("java-library")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    repositories {
        mavenLocal() // use mavenLocal to provide net.blueberrymc.minecraft:minecraft, for now.
        mavenCentral()
        maven { url = uri("https://repo.spongepowered.org/maven/"); name = "Sponge Powered" }
        maven { url = uri("https://jitpack.io"); name = "Jitpack" }
        maven { url = uri("https://libraries.minecraft.net/"); name = "Minecraft" }
        maven { url = uri("https://repo2.acrylicstyle.xyz/"); name = "acrylic-repo" }
    }

    dependencies {
        implementation(kotlin("stdlib", "1.6.0"))
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}

allprojects {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

    tasks {
        compileKotlin { kotlinOptions.jvmTarget = "17" }
        compileTestKotlin { kotlinOptions.jvmTarget = "17" }

        test {
            useJUnitPlatform()
        }

        withType<ProcessResources> {
            from(sourceSets.main.get().resources.srcDirs) {
                include("**")
                val tokenReplacementMap = mapOf("version" to project.version)
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }

        withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.isDeprecation = true
            // options.compilerArgs.add("-Xlint:unchecked")
        }
    }
}

subprojects {
    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.0")
        testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }
}

tasks {
    withType<net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft> {
        clientJar = "https://launcher.mojang.com/v1/objects/d49eb6caed53d23927648c97451503442f9e26fd/client.jar"
        clientObfuscationMap = "https://launcher.mojang.com/v1/objects/e824c89c612c0b9cb438ef739c44726c59bbf679/client.txt"
    }
}
