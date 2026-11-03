package com.tiagohenriques.project.timermasterlab.data.preferences

import okio.Path
import okio.Path.Companion.toPath
import java.io.File

actual fun getPreferencesFilePath(): Path {
    val osName = System.getProperty("os.name").lowercase()
    val appDataDirectory = when {
        osName.contains("win") -> System.getenv("APPDATA")?.let(::File)
        osName.contains("mac") -> File(System.getProperty("user.home"), "Library/Application Support")
        else -> System.getenv("XDG_DATA_HOME")?.let(::File)
            ?: File(System.getProperty("user.home"), ".local/share")
    } ?: error("Could not resolve the desktop preferences directory.")
    val appDirectory = File(appDataDirectory, "TimerMasterLab")
    check(appDirectory.exists() || appDirectory.mkdirs()) {
        "Could not create the desktop preferences directory: ${appDirectory.absolutePath}"
    }
    return File(appDirectory, "layout_preferences.preferences_pb").absolutePath.toPath()
}
