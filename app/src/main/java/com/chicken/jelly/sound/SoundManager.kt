package com.chicken.jelly.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.chicken.jelly.R

class SoundManager(private val context: Context) {

    private var musicPlayer: MediaPlayer? = null
    private var sfxPlayer: MediaPlayer? = null
    private var musicEnabled: Boolean = true
    private var soundEnabled: Boolean = true

    fun updateMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            musicPlayer?.pause()
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun playMenuMusic() {
        playMusic(R.raw.music_menu, looping = true)
    }

    fun playGameMusic() {
        playMusic(R.raw.music_game, looping = true)
    }

    private fun playMusic(@RawRes resId: Int, looping: Boolean) {
        if (!musicEnabled) return
        if (musicPlayer?.isPlaying == true) return
        musicPlayer?.release()
        musicPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = looping
            start()
        }
    }

    fun playEffect(@RawRes resId: Int) {
        if (!soundEnabled) return
        sfxPlayer?.release()
        sfxPlayer = MediaPlayer.create(context, resId).apply { start() }
    }

    fun pauseForLifecycle() {
        musicPlayer?.pause()
    }

    fun resumeAfterLifecycle() {
        if (musicEnabled) {
            musicPlayer?.start()
        }
    }

    fun release() {
        musicPlayer?.release()
        sfxPlayer?.release()
        musicPlayer = null
        sfxPlayer = null
    }
}
