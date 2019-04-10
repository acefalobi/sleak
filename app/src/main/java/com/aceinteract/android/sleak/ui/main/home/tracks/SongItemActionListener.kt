package com.aceinteract.android.sleak.ui.main.home.tracks

import com.aceinteract.android.sleak.data.entity.Song

interface SongItemActionListener {

    fun onSongClicked(song: Song)

    fun onSongLongClicked(song: Song): Boolean

}