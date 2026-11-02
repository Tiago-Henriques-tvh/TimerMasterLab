package com.tiagohenriques.project.timermasterlab.ui.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tiagohenriques.project.timermasterlab.data.StartListFileEntry
import com.tiagohenriques.project.timermasterlab.data.StartListJson
import com.tiagohenriques.project.timermasterlab.ui.subScreens.DraftStartListItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun writeSharedStartListFile(
    context: Context,
    mode: String,
    draft: List<DraftStartListItem>,
): File {
    val directory = File(context.cacheDir, "shared")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val safeMode = mode.replace(Regex("[^A-Za-z0-9]+"), "_")
    val file = File(directory, "startlist_${safeMode}_$stamp.json")
    val entries = draft.map { item ->
        StartListFileEntry(
            name = item.athlete.name,
            age = item.athlete.age,
            gender = item.athlete.gender,
            category = item.athlete.category,
            startOrder = item.startOrder,
            distanceMeters = item.distanceMeters,
        )
    }
    file.writeText(
        StartListJson.encode(
            mode = mode,
            entries = entries,
            exportedAtEpochMillis = System.currentTimeMillis(),
        ),
    )
    return file
}

fun shareStartListFile(context: Context, file: File) {
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Start list")
        putExtra(Intent.EXTRA_TITLE, file.name)
        clipData = ClipData.newRawUri(file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, "Share start list").apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooser)
}
