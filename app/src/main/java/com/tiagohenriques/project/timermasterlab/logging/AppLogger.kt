package com.tiagohenriques.project.timermasterlab.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * File logger that starts at process launch and fsyncs every line so a crash
 * does not drop the last records.
 */
object AppLogger {
    private const val PREFS_NAME = "app_logger"
    private const val KEY_ENABLED = "file_logging_enabled"
    private const val MAX_BYTES = 1_000_000

    private val lock = Any()
    private var logFile: File? = null
    private var handlerInstalled = false

    var isEnabled: Boolean = true
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isEnabled = prefs.getBoolean(KEY_ENABLED, true)
        val directory = File(appContext.filesDir, "logs")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        logFile = File(directory, "timermaster.log")
        installCrashHandler()
        i("AppLogger", "Logging initialized enabled=$isEnabled")
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        isEnabled = enabled
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .commit()
        i("AppLogger", "File logging ${if (enabled) "enabled" else "disabled"}")
    }

    fun i(tag: String, message: String) {
        log(level = "I", message = "$tag: $message", throwable = null, force = false)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(level = "E", message = "$tag: $message", throwable = throwable, force = false)
    }

    private fun installCrashHandler() {
        if (handlerInstalled) return
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(
                level = "CRASH",
                message = "Uncaught exception on ${thread.name}",
                throwable = throwable,
                force = isEnabled,
            )
            previous?.uncaughtException(thread, throwable)
        }
        handlerInstalled = true
    }

    private fun log(
        level: String,
        message: String,
        throwable: Throwable?,
        force: Boolean,
    ) {
        if (!isEnabled && !force) return
        val file = logFile ?: return
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = buildString {
            append(stamp)
            append(' ')
            append(level)
            append(' ')
            append(message)
            append('\n')
            if (throwable != null) {
                append(Log.getStackTraceString(throwable))
                append('\n')
            }
        }
        synchronized(lock) {
            runCatching {
                rotateIfNeeded(file)
                FileOutputStream(file, true).use { output ->
                    output.write(line.toByteArray(Charsets.UTF_8))
                    output.flush()
                    output.fd.sync()
                }
            }
        }
    }

    private fun rotateIfNeeded(file: File) {
        if (!file.exists() || file.length() < MAX_BYTES) return
        val backup = File(file.parentFile, "timermaster.log.1")
        if (backup.exists()) {
            backup.delete()
        }
        file.renameTo(backup)
    }
}
