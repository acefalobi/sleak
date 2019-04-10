package com.aceinteract.android.sleak.player

import android.content.ComponentName
import android.content.Context
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.aceinteract.android.sleak.service.MusicService
import kotlin.system.measureTimeMillis


open class MediaBrowserHelper(val context: Context, serviceClass: Class<MusicService>) {

    private val callbackList = ArrayList<MediaControllerCompat.Callback>()

    private var currentPlaylist: List<MediaBrowserCompat.MediaItem> = ArrayList()

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback()
    private val mediaControllerCallback = MediaControllerCallback()
    private val mediaBrowserSubscriptionCallback = MediaBrowserSubscriptionCallback()

    private val mediaBrowser = MediaBrowserCompat(context, ComponentName(context, serviceClass),
        mediaBrowserConnectionCallback, null)

    var mediaController: MediaControllerCompat? = null

    fun onStart() {
        if (!mediaBrowser.isConnected) mediaBrowser.connect()
    }

    fun onStop() {
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaController = null

        if (mediaBrowser.isConnected) mediaBrowser.disconnect()

        resetState()
    }

    open fun onConnected(mediaController: MediaControllerCompat) {}

    open fun onDisconnected() {}

    open fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {}

    private fun resetState() {
        performOnAllCallbacks(object : CallbackCommand {
            override fun perform(callback: MediaControllerCompat.Callback) {
                callback.onPlaybackStateChanged(null)
            }
        })
    }

    fun getTransportControls(): MediaControllerCompat.TransportControls = mediaController!!.transportControls

    fun registerCallback(callback: MediaControllerCompat.Callback) {
        callbackList.add(callback)

        // Update with the latest metadata/playback state.
        mediaController?.apply {
            val metadata = metadata
            if (metadata != null) {
                callback.onMetadataChanged(metadata)
            }

            val playbackState = playbackState
            if (playbackState != null) {
                callback.onPlaybackStateChanged(playbackState)
            }
        }
    }

    fun unregisterCallback(callback: MediaControllerCompat.Callback) {
        callbackList.remove(callback)
    }

    private fun performOnAllCallbacks(command: CallbackCommand) {
        for (callback in callbackList) command.perform(callback)
    }

    companion object {
        private val TAG = MediaBrowserHelper::class.simpleName
    }

    private interface CallbackCommand {
        fun perform(callback: MediaControllerCompat.Callback)
    }

    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            try {
                mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)

                mediaController?.let {
                    it.registerCallback(mediaControllerCallback)

                    mediaControllerCallback.onMetadataChanged(it.metadata)
                    mediaControllerCallback.onPlaybackStateChanged(it.playbackState)

                    this@MediaBrowserHelper.onConnected(it)
                }
            } catch (e: RemoteException) {
                throw RuntimeException(e)
            }

            mediaBrowser.subscribe(mediaBrowser.root, mediaBrowserSubscriptionCallback)
        }

    }

    open inner class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            this@MediaBrowserHelper.onChildrenLoaded(parentId, children)
            currentPlaylist = children
        }

    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            performOnAllCallbacks(object : CallbackCommand {
                override fun perform(callback: MediaControllerCompat.Callback) {
                    callback.onMetadataChanged(metadata)
                }
            })
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            performOnAllCallbacks(object : CallbackCommand {
                override fun perform(callback: MediaControllerCompat.Callback) {
                    callback.onPlaybackStateChanged(state)
                }
            })
        }

        override fun onSessionDestroyed() {
            resetState()
            onPlaybackStateChanged(null)

            this@MediaBrowserHelper.onDisconnected()
        }

    }

}