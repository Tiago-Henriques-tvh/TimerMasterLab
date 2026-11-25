package com.tiagohenriques.project.timermasterlab.platform

import kotlinx.datetime.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
