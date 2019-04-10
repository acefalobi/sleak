package com.aceinteract.android.sleak.ui.nowplaying

import android.app.Application
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.ui.base.BaseViewModel

class NowPlayingViewModel(application: Application) : BaseViewModel(application) {

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
