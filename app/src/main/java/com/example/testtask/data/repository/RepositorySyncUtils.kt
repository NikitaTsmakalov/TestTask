package com.example.testtask.data.repository

import kotlinx.coroutines.delay

object CachePolicy {
    const val LIST_TTL_MS: Long = 5 * 60 * 1000L
}

suspend fun <T> retryWithBackoff(
    attempts: Int = 3,
    initialDelayMs: Long = 400L,
    maxDelayMs: Long = 2_000L,
    block: suspend () -> T,
): T {
    var currentDelay = initialDelayMs
    var lastError: Throwable? = null

    repeat(attempts) { index ->
        try {
            return block()
        } catch (t: Throwable) {
            lastError = t
            val isLast = index == attempts - 1
            if (!isLast) {
                delay(currentDelay)
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
            }
        }
    }
    throw checkNotNull(lastError)
}

