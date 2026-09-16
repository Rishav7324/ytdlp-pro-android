package com.zyvro.app.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioFxManager private constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    var isEnabled: Boolean = true
        private set

    /** Master toggle: enables/disables every unit in the chain. */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer?.enabled = enabled
            }
        } catch (e: Exception) {
            Log.e("AudioFxManager", "setEnabled failed", e)
        }
    }

    /** Persisted volume-boost (millibels) re-applied on every new audio session. */
    var volumeBoostMb: Int = 0
        private set

    /** True when the platform granted a live effect chain (false = SFX unavailable). */
    fun hasActiveSession(): Boolean = equalizer != null

    fun initAudioEffects(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        try {
            release()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(500.toShort()) // 50% default
                }
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(300.toShort())
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = true
                    setTargetGain(volumeBoostMb)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioFxManager", "Failed to init audio effects", e)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000))
        } catch (e: Exception) {
            Log.e("AudioFxManager", "setBassBoost failed", e)
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        try {
            virtualizer?.setStrength(strength.coerceIn(0, 1000))
        } catch (e: Exception) {
            Log.e("AudioFxManager", "setVirtualizer failed", e)
        }
    }

    fun setVolumeGainMb(gainMb: Int) {
        volumeBoostMb = gainMb.coerceIn(0, 1000)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                loudnessEnhancer?.setTargetGain(volumeBoostMb)
            } catch (e: Exception) {
                Log.e("AudioFxManager", "setVolumeGainMb failed", e)
            }
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.e("AudioFxManager", "setBandLevel failed", e)
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
            equalizer = null
            bassBoost = null
            virtualizer = null
            loudnessEnhancer = null
        } catch (e: Exception) {
            Log.e("AudioFxManager", "release failed", e)
        }
    }

    companion object {
        val instance: AudioFxManager by lazy { AudioFxManager() }
    }
}
