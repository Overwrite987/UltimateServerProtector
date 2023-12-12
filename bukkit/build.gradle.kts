import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Locale
import ru.astrainteractive.gradleplugin.util.ProjectProperties.projectInfo

plugins {
    java
    `java-library`
    alias(libs.plugins.gradle.shadow)
    alias(libs.plugins.klibs.gradle.java.core)
}

dependencies {
    compileOnly(libs.minecraft.folia)
    implementation(libs.minecraft.bstats)
}

val processResources = project.tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(
            "name" to projectInfo.name,
            "version" to projectInfo.versionString,
        )
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}


// Creating shadow tasks for each locale
// ./gradlew :bukkit:shadowJarEn :bukkit:shadowJarRu
listOf("en", "ru").forEach { lang ->
    tasks.register<ShadowJar>("shadowJar${lang.toUpperCase(Locale.US)}") {
        dependsOn(processResources)
        sourceSets.main {
            resources {
                srcDirs("src/main/resources-$lang")
            }
        }
        from(sourceSets.main.get().output)
        isReproducibleFileOrder = true
        mergeServiceFiles()
        dependsOn(configurations)
        archiveClassifier.set(null as String?)
        relocate("org.bstats", projectInfo.group)
        // Skip version as ci not supports it
        archiveVersion.set("")
        archiveBaseName.set(projectInfo.name + "-$lang")
    }
}
