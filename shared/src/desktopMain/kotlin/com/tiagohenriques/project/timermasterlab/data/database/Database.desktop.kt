package com.tiagohenriques.project.timermasterlab.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val osName = System.getProperty("os.name").lowercase()
    val appDataDirectory = when {
        osName.contains("win") -> System.getenv("APPDATA")?.let(::File)
        osName.contains("mac") -> File(System.getProperty("user.home"), "Library/Application Support")
        else -> System.getenv("XDG_DATA_HOME")?.let(::File)
            ?: File(System.getProperty("user.home"), ".local/share")
    } ?: error("Could not resolve the desktop app data directory.")
    val appDirectory = File(appDataDirectory, "TimerMasterLab")
    check(appDirectory.exists() || appDirectory.mkdirs()) {
        "Could not create the desktop app data directory: ${appDirectory.absolutePath}"
    }
    return Room.databaseBuilder<AppDatabase>(
        name = File(appDirectory, "app_database").absolutePath,
    )
}
