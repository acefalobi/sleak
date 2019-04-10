package com.aceinteract.android.sleak.service

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.player.MediaPlayerAdapter
import com.aceinteract.android.sleak.player.PlaybackInfoListener
import com.aceinteract.android.sleak.player.PlayerAdapter
import com.aceinteract.android.sleak.service.notifications.MediaNotificationManager
import com.aceinteract.android.sleak.util.LocalLibraryUtil
import com.aceinteract.android.sleak.util.StorageUtil
import java.lang.RuntimeException


class MusicService : MediaBrowserServiceCompat() {

    private var hasServiceStarted = false

    private lateinit var localLibraryUtil: LocalLibraryUtil

    private lateinit var storageUtil: StorageUtil

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var playerAdapter: PlayerAdapter

    private lateinit var mediaNotificationManager: MediaNotificationManager

    private lateinit var sessionCallback: MediaSessionCompat.Callback

    override fun onCreate() {
        super.onCreate()

        localLibraryUtil = LocalLibraryUtil(this)
        storageUtil = StorageUtil(this)

        mediaSession = MediaSessionCompat(this, "MusicService")
        sessionCallback = MediaSessionCallback()

        mediaSession.setCallback(sessionCallback)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)


        sessionToken = mediaSession.sessionToken

        mediaNotificationManager = MediaNotificationManager(this)

        playerAdapter = MediaPlayerAdapter(this, MediaPlayerListener())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onLoadChildren(parentMediaId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val queue = storageUtil.queue

        if (queue.isNotEmpty()) {
            val song = localLibraryUtil.getSong(queue[storageUtil.currentQueuePosition].toLong())
            result.sendResult(
                arrayListOf(
                    MediaBrowserCompat.MediaItem(
                        localLibraryUtil.createMediaMetadataFromSong(song!!).description,
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                )
            )
        } else {
            result.sendResult(ArrayList())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerAdapter.stop()
        mediaSession.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? =
        BrowserRoot("root", null)

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        private val playlist = ArrayList<MediaSessionCompat.QueueItem>()

        private var songPosition = -1

        private var preparedMediaMetadata: MediaMetadataCompat? = null

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            if (playlist.size > 0) playlist.removeAll { true }
            playlist.add(MediaSessionCompat.QueueItem(description, description?.mediaId!!.toLong()))
            songPosition = if (songPosition == -1) 0 else songPosition

            mediaSession.setQueue(playlist)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            playlist.remove(playlist.find { it.description.mediaId == description?.mediaId })
            songPosition = if (playlist.isEmpty()) -1 else songPosition

            mediaSession.setQueue(playlist)
        }

        override fun onPrepare() {
            if (songPosition >= 0 && playlist.isNotEmpty()) {

                val mediaId = playlist[songPosition].description.mediaId
                preparedMediaMetadata = localLibraryUtil.getSongMetadata(mediaId!!.toLong())!!

                mediaSession.setMetadata(preparedMediaMetadata)

                if (!mediaSession.isActive) mediaSession.isActive = true

            }
        }

        override fun onPlay() {
            if (isReadyToPlay()) {

                if (preparedMediaMetadata == null) onPrepare()

                try {
                    playerAdapter.playFromMedia(preparedMediaMetadata!!)
                } catch (e: RuntimeException) {
                    Toast.makeText(this@MusicService, R.string.error_playing_song, Toast.LENGTH_LONG).show()
                    onSkipToNext()
                }

            }
        }

        override fun onPause() {
            playerAdapter.pause()
        }

        override fun onStop() {
            playerAdapter.stop()
            mediaSession.isActive = false
        }

        override fun onSkipToNext() {
            val queue = storageUtil.queue
            val queuePosition = storageUtil.currentQueuePosition

            val newQueuePosition = if (queue.size > queuePosition) queuePosition + 1 else 0
            val nextSongId = queue[newQueuePosition]
            storageUtil.currentQueuePosition = newQueuePosition

            onRemoveQueueItem(playlist[0].description)
            onAddQueueItem(localLibraryUtil.getSongMetadata(nextSongId.toLong())!!.description)

            preparedMediaMetadata = null
            onPlay()
        }

        override fun onSkipToPrevious() {
            val queue = storageUtil.queue
            val queuePosition = storageUtil.currentQueuePosition

            val prevQueuePosition = if (queuePosition > 0) 0 else queue.size - 1
            val prevSongId = queue[prevQueuePosition]
            storageUtil.currentQueuePosition = prevQueuePosition

            onRemoveQueueItem(playlist[0].description)
            onAddQueueItem(localLibraryUtil.getSongMetadata(prevSongId.toLong())!!.description)

            preparedMediaMetadata = null
            onPlay()
        }

        override fun onSeekTo(pos: Long) {
            playerAdapter.seekTo(pos.toInt())
        }

        private fun isReadyToPlay(): Boolean = !playlist.isEmpty()

    }

    internal inner class MediaPlayerListener : PlaybackInfoListener() {

        private val serviceManager = ServiceManager()

        override fun onDurationChanged(duration: Long) {

        }

        override fun onPositionChanged(currentPosition: Int) {

        }

        override fun onPlaybackCompleted() {
            sessionCallback.onSkipToNext()
        }

        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            mediaSession.setPlaybackState(state)

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> serviceManager.moveServiceToStartedState(state)
                PlaybackStateCompat.STATE_PAUSED -> serviceManager.updateNotificationForPause(state)
                PlaybackStateCompat.STATE_STOPPED -> serviceManager.moveServiceOutOfStartedState()
            }
        }

        inner class ServiceManager {

            internal fun moveServiceToStartedState(state: PlaybackStateCompat) {
                val notification = mediaNotificationManager.getNotification(playerAdapter.currentMedia,
                    state, sessionToken!!)

                if (!hasServiceStarted) {
                    ContextCompat.startForegroundService(this@MusicService,
                        Intent(this@MusicService, MusicService::class.java))
                    hasServiceStarted = true
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification)

            }

            internal fun updateNotificationForPause(state: PlaybackStateCompat) {
                stopForeground(false)
                val notification = mediaNotificationManager.getNotification(playerAdapter.currentMedia,
                    state, sessionToken!!)

                mediaNotificationManager.notificationManager
                    .notify(MediaNotificationManager.NOTIFICATION_ID, notification)
            }

            internal fun moveServiceOutOfStartedState() {
                stopForeground(true)
                stopSelf()
                hasServiceStarted = false
            }

        }

    }

}