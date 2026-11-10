package com.cdpg.dhruva.core.common

import kotlin.time.Clock
import kotlin.time.Instant

/** Injectable clock so cache freshness and timestamps are testable. */
fun interface AppClock {
    fun now(): Instant

    companion object {
        val System: AppClock = AppClock { Clock.System.now() }
    }
}
