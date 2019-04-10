package com.aceinteract.android.sleak

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.support.multidex.MultiDexApplication
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import com.aceinteract.android.sleak.player.MediaBrowserHelper
import com.aceinteract.android.sleak.service.MusicService
import com.aceinteract.android.sleak.util.LocalLibraryUtil
import com.aceinteract.android.sleak.util.StorageUtil
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class SleakApplication : MultiDexApplication() {

    private lateinit var localLibraryUtil: LocalLibraryUtil
    private lateinit var storageUtil: StorageUtil

    lateinit var mediaBrowserHelper: MediaBrowserHelper

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        localLibraryUtil = LocalLibraryUtil(this)
        storageUtil = StorageUtil(this)

        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            startMediaBrowserConnection()

        CalligraphyConfig.initDefault(
            CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                .setFontAttrId(uk.co.chrisjenx.calligraphy.R.attr.fontPath)
                .build())
    }

    override fun onTerminate() {
        super.onTerminate()
        mediaBrowserHelper.onStop()
    }

    fun startMediaBrowserConnection() {
        mediaBrowserHelper = MediaBrowserConnection(this)
        mediaBrowserHelper.onStart()
    }

    fun getTransportControls() = mediaBrowserHelper.getTransportControls()

    private inner class MediaBrowserConnection(context: Context)
        : MediaBrowserHelper(context, MusicService::class.java) {

        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            super.onChildrenLoaded(parentId, children)

            val mediaController = this.mediaController!!

            for (mediaItem in children) {
                mediaController.addQueueItem(mediaItem.description)
            }
        }

    }

    companion object {

        @get:Synchronized
        var INSTANCE: SleakApplication? = null
            private set

        const val MUSIC_NOTIFICATION_ID = 9999

        const val PERMISSION_WRITE_READ_EXTERNAL_STORAGE = 1000

        const val MAIN_BROADCAST = "com.aceinteract.android.sleak.MAIN_BROADCAST"

        const val BROADCAST_IS_PLAYING = "is_playing"
        const val BROADCAST_TITLE = "title"
        const val BROADCAST_ARTISTS = "artists"
        const val BROADCAST_ARTIST_IDS = "artist_ids"
        const val BROADCAST_SONG_ID = "song_id"
        const val BROADCAST_ALBUM_TITLE = "album_title"
        const val BROADCAST_ALBUM_ID = "album_id"

        // const val baseUrl = "http://192.168.43.189:8080/"
        const val baseUrl = "http://192.168.240.2:8080/"

    }
}