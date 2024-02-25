buildscript {
    dependencies {
        classpath("ru.astrainteractive.gradleplugin:convention:0.5.1")
    }
}

plugins {
    java
    `java-library`
	`maven-publish`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.klibs.gradle.java.core) apply false
    alias(libs.plugins.klibs.gradle.rootinfo)
    alias(libs.plugins.gradle.shadow) apply false
}
