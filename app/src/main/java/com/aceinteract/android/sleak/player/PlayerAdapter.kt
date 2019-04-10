package com.aceinteract.android.sleak.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.MediaMetadataCompat


abstract class PlayerAdapter(context: Context) {

    private var isAudioNoisyReceiverRegistered = false
    private val audioNoisyReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                if (isPlaying) pause()
            }
        }

    }

    private val applicationContext: Context = context.applicationContext
    private val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusHelper: AudioFocusHelper = AudioFocusHelper()

    private var playOnAudioFocus = false

    abstract val isPlaying: Boolean

    abstract var currentMedia: MediaMetadataCompat

    abstract fun playFromMedia(metadata: MediaMetadataCompat)

    fun play() {
        if (audioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver()

            onPlay()
        }
    }

    protected abstract fun onPlay()

    fun pause() {
        if (!playOnAudioFocus) audioFocusHelper.abandonAudioFocus()
        unregisterAudioNoisyReceiver()
        onPause()
    }


    protected abstract fun onPause()

    fun stop() {
        audioFocusHelper.abandonAudioFocus()
        unregisterAudioNoisyReceiver()
        onStop()
    }

    protected abstract fun onStop()

    abstract fun initializeProgressCallback()

    abstract fun seekTo(position: Int)

    abstract fun setVolume(volume: Float)

    private fun registerAudioNoisyReceiver() {
        if (!isAudioNoisyReceiverRegistered) {
            applicationContext.registerReceiver(audioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER)
            isAudioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (isAudioNoisyReceiverRegistered) {
            applicationContext.unregisterReceiver(audioNoisyReceiver)
            isAudioNoisyReceiverRegistered = false
        }
    }

    companion object {

        private const val MEDIA_VOLUME_DEFAULT = 1.0f
        private const val MEDIA_VOLUME_DUCK = 0.2f

        private val AUDIO_NOISY_INTENT_FILTER = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    }

    /**
     * Helper class for managing audio focus related tasks.
     */
    private inner class AudioFocusHelper : AudioManager.OnAudioFocusChangeListener {

        fun requestAudioFocus(): Boolean {
            val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        fun abandonAudioFocus() {
            audioManager.abandonAudioFocus(this)
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (playOnAudioFocus && !isPlaying) {
                        play()
                    } else if (isPlaying) {
                        setVolume(MEDIA_VOLUME_DEFAULT)
                    }
                    playOnAudioFocus = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> setVolume(MEDIA_VOLUME_DUCK)
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying) {
                    playOnAudioFocus = true
                    pause()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    audioManager.abandonAudioFocus(this)
                    playOnAudioFocus = false
                    stop()
                }
            }
        }
    }
}