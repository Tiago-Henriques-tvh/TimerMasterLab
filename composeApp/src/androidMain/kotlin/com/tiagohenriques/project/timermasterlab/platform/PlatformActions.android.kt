package com.tiagohenriques.project.timermasterlab.platform

import android.content.Intent
import androidx.core.content.FileProvider
import com.tiagohenriques.project.timermasterlab.data.database.currentApplicationContext
import java.io.File

actual suspend fun shareFile(name: String, bytes: ByteArray, mimeType: String) {
    val context = checkNotNull(currentApplicationContext()) {
        "Initialize the Android application context before sharing files."
    }
    val directory = File(context.cacheDir, "shared")
    check(directory.exists() || directory.mkdirs()) {
        "Could not create the share directory."
    }
    val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val file = File(directory, safeName)
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, name)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "Share $name")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION),
    )
}

actual fun openExternalUrl(url: String) {
    val context = checkNotNull(currentApplicationContext()) {
        "Initialize the Android application context before opening links."
    }
    context.startActivity(
        Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
