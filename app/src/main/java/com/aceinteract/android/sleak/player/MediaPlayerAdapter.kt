package com.aceinteract.android.sleak.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import android.os.SystemClock
import com.aceinteract.android.sleak.util.LocalLibraryUtil


class MediaPlayerAdapter(val context: Context, val listener: PlaybackInfoListener) : PlayerAdapter(context) {

    private var songUri: Uri? = null

    private val localLibraryUtil = LocalLibraryUtil(context)

    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private var state = PlaybackStateCompat.STATE_NONE

    private var hasCurrentMediaPlayedToCompletion = false

    override lateinit var currentMedia: MediaMetadataCompat

    override val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    private var executor: ScheduledExecutorService? = null

    private var seekBarPositionUpdateTask: Runnable? = null

    private var seekWhileNotPlaying = -1

    init {
        mediaPlayer.setOnCompletionListener {
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            listener.onPlaybackCompleted()
        }
    }

    override fun playFromMedia(metadata: MediaMetadataCompat) {
        currentMedia = metadata
        val mediaId = metadata.description.mediaId
        val song = localLibraryUtil.getSong(mediaId!!.toLong())
        song?.apply { playSong((trackUri)) }
    }

    private fun playSong(uri: Uri) {
        var hasMediaChanged = uri.toString() != songUri.toString()

        if (hasCurrentMediaPlayedToCompletion) {
            hasMediaChanged = true
            hasCurrentMediaPlayedToCompletion = false
        }

        if (!hasMediaChanged) {
            if (!isPlaying) play()
            return
        } else reset()

        songUri = uri

        try {
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepare()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        play()
    }

    override fun onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        stopUpdatingCallbackWithPosition(true)
        reset()
    }

    private fun reset() {
        mediaPlayer.reset()
    }

    override fun onPlay() {
        mediaPlayer.start()
        setNewState(PlaybackStateCompat.STATE_PLAYING)
        startUpdatingCallbackWithPosition()
    }

    override fun onPause() {
        mediaPlayer.pause()
        setNewState(PlaybackStateCompat.STATE_PAUSED)
    }

    private fun setNewState(@PlaybackStateCompat.State newState: Int) {
        state = newState

        if (state == PlaybackStateCompat.STATE_STOPPED) hasCurrentMediaPlayedToCompletion = true

        val reportPosition: Int
        if (seekWhileNotPlaying >= 0) {
            reportPosition = seekWhileNotPlaying
            if (state == PlaybackStateCompat.STATE_PLAYING) seekWhileNotPlaying = -1
        } else reportPosition = mediaPlayer.currentPosition

        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setActions(getAvailableActions())
        stateBuilder.setState(state, reportPosition.toLong(), 1.0f, SystemClock.elapsedRealtime()
        )

        listener.onPlaybackStateChange(stateBuilder.build())
    }

    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long {
        var actions = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        actions = when (state) {
            PlaybackStateCompat.STATE_STOPPED -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PAUSE)
            PlaybackStateCompat.STATE_PLAYING -> actions or (PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_SEEK_TO)
            PlaybackStateCompat.STATE_PAUSED -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_STOP)
            else -> actions or (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE)
        }
        return actions
    }

    override fun initializeProgressCallback() {
        listener.onDurationChanged(mediaPlayer.duration.toLong())
    }

    override fun seekTo(position: Int) {
        if (!mediaPlayer.isPlaying) seekWhileNotPlaying = position

        mediaPlayer.seekTo(position)

        setNewState(state)
    }

    override fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }

    private fun updateProgressCallbackTask() {
        if (isPlaying) {
            val currentPosition = mediaPlayer.currentPosition
            listener.onPositionChanged(currentPosition)
        }
    }

    private fun startUpdatingCallbackWithPosition() {

        executor = Executors.newSingleThreadScheduledExecutor()

        seekBarPositionUpdateTask = Runnable {
            updateProgressCallbackTask()
        }

        executor!!.scheduleAtFixedRate(seekBarPositionUpdateTask, 0, PLAYBACK_POSITION_REFRESH_INTERVAL, TimeUnit.MILLISECONDS)

    }

    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {

        executor?.apply {
            shutdownNow()
            executor = null
            seekBarPositionUpdateTask = null
            if (resetUIPlaybackPosition ) {
                listener.onPositionChanged(0)
            }
        }

    }

    companion object {
        const val PLAYBACK_POSITION_REFRESH_INTERVAL: Long = 1000
    }
}