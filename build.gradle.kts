// Define android subprojects here
// Global configurations and plugins class paths

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Static code analysis tools
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
    // Code quality tool for Kotlin
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    dependencies {
        classpath(libs.hilt.android.gradle.plugin)
    }

    extra.set("room_version", "2.6.0")
}
