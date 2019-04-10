package com.aceinteract.android.sleak.ui.base

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableLong
import android.support.v4.media.MediaBrowserCompat
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.util.LocalLibraryUtil
import com.aceinteract.android.sleak.util.SingleLiveEvent
import com.aceinteract.android.sleak.util.StorageUtil

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    protected val context = application.applicationContext!!

    protected val storageUtil = StorageUtil(context)
    protected val localLibraryUtil = LocalLibraryUtil(context)

    val playingSong = ObservableLong()

    val snackbarMessage = SingleLiveEvent<Int>()

    fun showSnackbarMessage(message: Int) {
        snackbarMessage.value = message
    }

    fun loadNewPlaylist(songs: List<Song>, songPosition: Int) {

        val queue = ArrayList<String>()

        songs.forEach { queue.add(it.id.toString()) }

        storageUtil.queue = queue
        storageUtil.currentQueuePosition = songPosition

        SleakApplication.INSTANCE?.mediaBrowserHelper?.onChildrenLoaded(
            "root",
            arrayListOf(
                MediaBrowserCompat.MediaItem(
                    localLibraryUtil.createMediaMetadataFromSong(songs[songPosition]).description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            )
        )
        SleakApplication.INSTANCE?.getTransportControls()?.prepare()
        SleakApplication.INSTANCE?.getTransportControls()?.play()
    }

}