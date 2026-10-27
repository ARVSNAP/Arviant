package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundEffectManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap: Map<Int, Int>

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        soundMap = mutableMapOf()
    }

    fun loadSounds(context: Context, sounds: Map<String, Int>) {
        sounds.forEach { (name, resId) ->
            (soundMap as MutableMap)[resId] = soundPool.load(context, resId, 1)
        }
    }

    fun playSound(resId: Int) {
        val soundId = soundMap[resId] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
