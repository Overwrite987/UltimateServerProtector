plugins {
    java
    id("com.gradleup.shadow") version "9.1.0"
}

group = "ru.overwrite.protect"
version = "36.0"
description = "UltimateServerProtector Plugin"

val lang = project.findProperty("lang")?.toString() ?: "ru"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")

    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    processResources {
        val props = mapOf(
            "version" to version,
            "name" to project.name,
            "description" to project.description
        )

        inputs.properties(props)
        filteringCharset = "UTF-8"

        from("src/main/resources-common") {
            expand(props)
        }
        from("src/main/resources-${lang}") {
            expand(props)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveBaseName.set("UltimateServerProtector")
        archiveVersion.set("v${version}-${lang}")
        archiveClassifier.set("")

        relocate("org.bstats", "ru.overwrite.protect.bukkit.utils.metrics")

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}