package com.aceinteract.android.sleak.ui.main

import android.app.Application
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.v4.media.MediaBrowserCompat
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.ui.base.BaseViewModel

class MainViewModel(application: Application) : BaseViewModel(application) {

    val isPlaying = ObservableBoolean(false)

    val song = ObservableField<Song>()

    fun start() {
        val queue = storageUtil.queue
        playingSong.set(queue[storageUtil.currentQueuePosition].toLong())
        if (queue.isNotEmpty()) loadSong()
    }

    fun loadSong() {
        song.set(localLibraryUtil.getSong(playingSong.get()))
    }

}