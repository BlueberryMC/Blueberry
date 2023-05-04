import net.blueberrymc.gradle.buildSrc.Util.getBuildNumber
import net.blueberrymc.gradle.buildSrc.constants.KOTLIN_VERSION
import net.blueberrymc.gradle.buildSrc.constants.API_VERSION
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION

plugins {
    java
    kotlin("jvm") version net.blueberrymc.gradle.buildSrc.constants.KOTLIN_VERSION
    `maven-publish`
    `java-library`
}

apply<net.blueberrymc.gradle.buildSrc.BuildPlugin>()

group = "net.blueberrymc"
version = "dev-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks {
    register("printVersion") {
        group = "blueberry"

        doLast {
            if (project.properties["BUILD_NUMBER"].toString().toLongOrNull() != null) {
                println("$MINECRAFT_VERSION-${API_VERSION.replace("-SNAPSHOT", "")}.${getBuildNumber(project)}")
            } else {
                println("$MINECRAFT_VERSION-${API_VERSION.replace("-SNAPSHOT", "")}")
            }
        }
    }
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
        mavenCentral()
        maven { url = uri("https://jitpack.io"); name = "Jitpack" }
        maven { url = uri("https://libraries.minecraft.net/"); name = "Minecraft" }
        maven { url = uri("https://repo.spongepowered.org/maven/"); name = "Sponge Powered" }
        maven { url = uri("https://repo.blueberrymc.net/repository/maven-public/"); name = "blueberrymc-repo" }
        mavenLocal() // use mavenLocal to provide net.blueberrymc.minecraft:minecraft, for now.
    }

    dependencies {
        implementation(kotlin("stdlib", KOTLIN_VERSION))
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "blueberryRepo"
                credentials(PasswordCredentials::class)
                url = uri(
                    if (API_VERSION.endsWith("-SNAPSHOT"))
                        "https://repo.blueberrymc.net/repository/maven-snapshots/"
                    else
                        "https://repo.blueberrymc.net/repository/maven-releases/"
                )
            }
        }
    }

    if (name == "blueberry") {
        tasks.names.forEach { taskName ->
            if (taskName.startsWith("publish")) {
                tasks.named(taskName).get().enabled = false
            }
        }
    }
}

allprojects {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }

        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }

        javadoc {
            options.encoding = "UTF-8"
        }

        compileJava {
            options.compilerArgs.add("-Xmaxerrs")
            options.compilerArgs.add("99999")
            options.encoding = "UTF-8"
            options.isDeprecation = true
        }

        test {
            useJUnitPlatform()
        }

        processResources {
            /*
            from(sourceSets.main.get().resources.srcDirs) {
                include("**")
                val tokenReplacementMap = mapOf("version" to project.version)
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            */
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }
    }
}

subprojects {
    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$KOTLIN_VERSION")
        testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }
}
