package com.aceinteract.android.sleak.ui.nowplaying

interface NowPlayingItemActionListener {

    fun onPlayPauseClicked(isPlaying: Boolean)

    fun onNextClicked()

    fun onPrevClicked()

    fun onQueueClicked()

}