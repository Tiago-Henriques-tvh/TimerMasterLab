// settings.gradle is used to configure the build environment and include modules in a Gradle
// project. Defining the root project name and specifying which subprojects (modules) are part of
// the build. It also organize and manage dependencies for multi-module projects.

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // added so libraries hosted on JitPack (e.g. burnoutcrew/reorderable) can be resolved
    }
}

rootProject.name = "TimerMasterLab"
include(":app")
include(":composeApp")
include(":shared")
