package com.tiagohenriques.project.timermasterlab.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private var applicationContext: Context? = null

fun initializeDatabaseContext(context: Context) {
    applicationContext = context.applicationContext
}

fun currentApplicationContext(): Context? = applicationContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = checkNotNull(applicationContext) {
        "Initialize the database context before creating the app container."
    }
    return Room.databaseBuilder(
        context = context,
        name = context.getDatabasePath("app_database").absolutePath,
    )
}
