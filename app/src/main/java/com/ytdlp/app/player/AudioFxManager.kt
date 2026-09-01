package com.ytdlp.app.player

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
                    setTargetGain(0)
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                loudnessEnhancer?.setTargetGain(gainMb.coerceIn(0, 1000))
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
