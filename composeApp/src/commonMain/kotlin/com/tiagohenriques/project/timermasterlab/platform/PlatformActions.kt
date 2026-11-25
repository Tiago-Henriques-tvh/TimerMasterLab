package com.tiagohenriques.project.timermasterlab.platform

expect suspend fun shareFile(name: String, bytes: ByteArray, mimeType: String)

expect fun openExternalUrl(url: String)
