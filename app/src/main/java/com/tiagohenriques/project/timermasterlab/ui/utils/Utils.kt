package com.tiagohenriques.project.timermasterlab.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Utils {

    companion object {
        fun formatDuration(millis: Long?): String {
            if (millis == null || millis <= 0L) return "--:--:--"
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return "%02d.%02d.%02d".format(hours, minutes, seconds)
        }

        fun formatSessionDate(dateStr: String): String {
            if (dateStr.isBlank()) return ""
            try {
                val epoch = dateStr.toLong()
                val instant = Instant.ofEpochMilli(epoch)
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
                return fmt.format(instant)
            } catch (_: Throwable) {
            }

            try {
                val instant = Instant.parse(dateStr)
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
                return fmt.format(instant)
            } catch (_: Throwable) {
            }

            return dateStr
        }
    }
}

