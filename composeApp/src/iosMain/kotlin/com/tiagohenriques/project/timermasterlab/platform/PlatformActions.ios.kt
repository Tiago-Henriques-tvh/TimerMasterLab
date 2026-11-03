package com.tiagohenriques.project.timermasterlab.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual suspend fun shareFile(name: String, bytes: ByteArray, mimeType: String) {
    val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val path = NSTemporaryDirectory() + safeName
    val data = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    check(data.writeToFile(path, atomically = true)) { "Could not create the file to share." }
    val fileUrl = NSURL.fileURLWithPath(path)
    val controller = UIActivityViewController(
        activityItems = listOf(fileUrl),
        applicationActivities = null,
    )
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController
    val presenter = root?.presentedViewController ?: root
    checkNotNull(presenter) { "Could not find an iOS view controller to present sharing." }
    controller.popoverPresentationController?.sourceView = presenter.view
    controller.popoverPresentationController?.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
    presenter.presentViewController(controller, animated = true, completion = null)
}

actual fun openExternalUrl(url: String) {
    val nsUrl = checkNotNull(NSURL.URLWithString(url)) { "Invalid URL." }
    check(UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        "No iOS app can open this link."
    }
    UIApplication.sharedApplication.openURL(nsUrl)
}
