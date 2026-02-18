// Here the plugin is applied
// A plugin is an extension to the build system that can modify the build process
// and add new capabilities. Plugins can be used to automate tasks, enforce conventions,
// and integrate with other tools and services. While a SDK's is a set of tools and libraries
// that developers use to build applications for a specific platform or framework.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

apply<HelloWorldPlugin>()

class HelloWorldPlugin : Plugin<Project> {
    var message: String = "Hello from HelloWorldPlugin!"

    override fun apply(target: Project) {
        println(message)
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.test.espresso:espresso-core:3.5.0")
    }
}

android {
    namespace = "com.tiagohenriques.project.timermasterlab"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tiagohenriques.project.timermasterlab"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

detekt {
    config.from(files("$rootDir/detekt.yml")) // Custom configuration file
    buildUponDefaultConfig = true
}

ktlint {
    enableExperimentalRules.set(true)
    android.set(true)
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.named("check") {
    dependsOn("ktlintFormat") // autoformat code
    dependsOn("ktlintCheck") // verify all rules
    dependsOn("detekt") // static code analysis
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.google.android.gms.play.services.location)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hilt.android) // Hilt runtime
    kapt(libs.hilt.android.compiler) // Hilt annotation processor
    implementation(libs.androidx.hilt.lifecycle.viewmodel) // Hilt ViewModel
    kapt(libs.androidx.hilt.compiler) // Hilt compiler
    implementation(libs.androidx.hilt.navigation.compose) // Compose Hilt integration

    implementation(libs.retrofit) // Retrofit for network requests
    implementation(libs.converter.gson) // Gson converter for Retrofit

    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core.android)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Use ksp for the compiler
    testImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.rxjava3)

    implementation(libs.coil.compose)
    implementation(libs.bundles.ktor)

    implementation(libs.androidx.biometric)
    implementation(kotlin("test"))

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
}
