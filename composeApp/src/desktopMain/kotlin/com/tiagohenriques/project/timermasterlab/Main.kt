package com.tiagohenriques.project.timermasterlab

import io.github.vinceglb.filekit.FileKit
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    FileKit.init(appId = "com.tiagohenriques.project.timermasterlab")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Timer Master Lab",
    ) {
        App()
    }
}
