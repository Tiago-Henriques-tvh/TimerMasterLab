package com.tiagohenriques.project.timermasterlab.platform

import java.awt.Desktop
import java.io.File
import java.net.URI

actual suspend fun shareFile(name: String, bytes: ByteArray, mimeType: String) {
    check(Desktop.isDesktopSupported()) {
        "This desktop environment cannot open files for sharing."
    }
    val desktop = Desktop.getDesktop()
    check(desktop.isSupported(Desktop.Action.OPEN)) {
        "This desktop environment has no application registered to open shared files."
    }
    val directory = File(System.getProperty("java.io.tmpdir"), "TimerMasterLab/shared")
    check(directory.exists() || directory.mkdirs()) {
        "Could not create the temporary share directory."
    }
    val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val file = File(directory, safeName)
    file.writeBytes(bytes)
    desktop.open(file)
}

actual fun openExternalUrl(url: String) {
    check(Desktop.isDesktopSupported()) { "This desktop environment cannot open links." }
    val desktop = Desktop.getDesktop()
    check(desktop.isSupported(Desktop.Action.BROWSE)) {
        "This desktop environment has no web browser registered."
    }
    desktop.browse(URI(url))
}
