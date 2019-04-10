package com.aceinteract.android.sleak.player

import android.support.v4.media.session.PlaybackStateCompat


abstract class PlaybackInfoListener {

    abstract fun onPlaybackStateChange(state: PlaybackStateCompat)

    open fun onPlaybackCompleted() {}

    abstract fun onDurationChanged(duration: Long)

    abstract fun onPositionChanged(currentPosition: Int)


}