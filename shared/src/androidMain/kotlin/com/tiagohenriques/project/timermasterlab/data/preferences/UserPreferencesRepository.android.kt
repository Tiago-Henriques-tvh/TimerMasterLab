package com.tiagohenriques.project.timermasterlab.data.preferences

import okio.Path
import okio.Path.Companion.toPath

actual fun getPreferencesFilePath(): Path {
    val context = checkNotNull(
        com.tiagohenriques.project.timermasterlab.data.database.currentApplicationContext(),
    ) {
        "Initialize the Android application context before creating preferences."
    }
    return context.filesDir.resolve("datastore/layout_preferences.preferences_pb")
        .absolutePath.toPath()
}
