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

    private var currentMusicRes: Int? = null
    private var currentMusicLooping: Boolean = false

    fun updateMusicEnabled(enabled: Boolean) {
        val wasEnabled = musicEnabled
        musicEnabled = enabled
        if (!enabled) {
            musicPlayer?.pause()
            isAutoPaused = false
        } else if (!wasEnabled || musicPlayer?.isPlaying != true) {
            currentMusicRes?.let { playMusic(it, currentMusicLooping) }
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
        val resChanged = currentMusicRes != resId
        currentMusicRes = resId
        currentMusicLooping = looping

        if (!musicEnabled) return
        if (isLifecyclePaused) {
            isAutoPaused = true
            return
        }
        if (musicPlayer?.isPlaying == true && !resChanged) return

        if (!resChanged && musicPlayer != null) {
            musicPlayer?.start()
            isAutoPaused = false
            return
        }

        musicPlayer?.release()
        musicPlayer =
                MediaPlayer.create(context, resId)?.apply {
                    isLooping = looping
                    start()
                }
        isAutoPaused = false
    }

    fun playEffect(@RawRes resId: Int) {
        if (!soundEnabled || isLifecyclePaused) return
        sfxPlayer?.release()
        sfxPlayer = MediaPlayer.create(context, resId)?.apply { start() }
    }

    private var isAutoPaused: Boolean = false
    private var isLifecyclePaused: Boolean = false

    fun pauseForLifecycle() {
        isLifecyclePaused = true
        if (musicPlayer?.isPlaying == true) {
            musicPlayer?.pause()
            isAutoPaused = true
        }
    }

    fun resumeAfterLifecycle() {
        isLifecyclePaused = false
        if (musicEnabled && isAutoPaused) {
            currentMusicRes?.let { playMusic(it, currentMusicLooping) }
        }
    }

    fun release() {
        musicPlayer?.release()
        sfxPlayer?.release()
        musicPlayer = null
        sfxPlayer = null
    }
}
