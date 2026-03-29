package com.example.testtask.presentation.characters

import androidx.annotation.StringRes
import com.example.testtask.R

data class NetworkRecoveryEffect(
    @StringRes val bannerResId: Int? = null,
    val clearError: Boolean = false,
)

class NetworkRecoveryStateMachine(
    val offlineStabilizeMs: Long = 1_500L,
    val onlineStabilizeMs: Long = 1_000L,
) {
    companion object {
        @StringRes
        val OFFLINE_BANNER = R.string.banner_offline_cached

        @StringRes
        val RESTORED_BANNER = R.string.banner_restored
    }

    private var lastStableOnline: Boolean? = null
    private var hadOfflineSignal: Boolean = false
    private var hadNetworkFailure: Boolean = false

    fun stabilizeDelayMs(rawOnline: Boolean): Long =
        if (rawOnline) onlineStabilizeMs else offlineStabilizeMs

    fun markRefreshFailed() {
        hadNetworkFailure = true
    }

    fun hasNetworkFailure(): Boolean = hadNetworkFailure

    fun hasRecoveryPending(): Boolean = hadNetworkFailure || hadOfflineSignal

    fun onRecoveryProbeSuccess(): NetworkRecoveryEffect {
        if (!hasRecoveryPending()) return NetworkRecoveryEffect()
        hadOfflineSignal = false
        hadNetworkFailure = false
        return NetworkRecoveryEffect(
            bannerResId = RESTORED_BANNER,
            clearError = true,
        )
    }

    fun onRefreshSuccess(isOnline: Boolean): NetworkRecoveryEffect {
        if (hadNetworkFailure && isOnline) {
            hadNetworkFailure = false
            return NetworkRecoveryEffect(
                bannerResId = RESTORED_BANNER,
                clearError = true,
            )
        }
        return if (isOnline) {
            NetworkRecoveryEffect(
                bannerResId = R.string.banner_data_updated,
                clearError = true,
            )
        } else {
            NetworkRecoveryEffect()
        }
    }

    fun onStableNetwork(rawOnline: Boolean, cacheErrorPresent: Boolean): NetworkRecoveryEffect {
        val previousStable = lastStableOnline
        if (previousStable == null) {
            lastStableOnline = rawOnline
            return if (!rawOnline) {
                hadOfflineSignal = true
                NetworkRecoveryEffect(bannerResId = OFFLINE_BANNER, clearError = true)
            } else {
                NetworkRecoveryEffect()
            }
        }

        if (rawOnline != previousStable) {
            lastStableOnline = rawOnline
            return if (rawOnline) {
                val shouldShowRestored = hadOfflineSignal || hadNetworkFailure
                hadOfflineSignal = false
                hadNetworkFailure = false
                if (shouldShowRestored) {
                    NetworkRecoveryEffect(bannerResId = RESTORED_BANNER, clearError = true)
                } else {
                    NetworkRecoveryEffect()
                }
            } else {
                hadOfflineSignal = true
                NetworkRecoveryEffect(bannerResId = OFFLINE_BANNER, clearError = true)
            }
        }

        if (rawOnline && (cacheErrorPresent || hadNetworkFailure)) {
            hadOfflineSignal = false
            hadNetworkFailure = false
            return NetworkRecoveryEffect(
                bannerResId = RESTORED_BANNER,
                clearError = true,
            )
        }

        return NetworkRecoveryEffect()
    }
}

