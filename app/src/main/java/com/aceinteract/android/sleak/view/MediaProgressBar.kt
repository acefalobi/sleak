package com.aceinteract.android.sleak.view

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar



class MediaProgressBar(context: Context, attrs: AttributeSet) : ProgressBar(context, attrs) {

    private var mediaController: MediaControllerCompat? = null
    private var controllerCallback: ControllerCallback = ControllerCallback()
    private var progressAnimator: ValueAnimator? = null

    private var isTracking = false

    fun setMediaController(mediaController: MediaControllerCompat) {
        controllerCallback = ControllerCallback()
        mediaController.registerCallback(controllerCallback)
        this.mediaController = mediaController
        controllerCallback.onMetadataChanged(mediaController.metadata)
        controllerCallback.onPlaybackStateChanged(mediaController.playbackState)
    }

    fun disconnectController() {
        mediaController?.apply {
            unregisterCallback(controllerCallback)
            this@MediaProgressBar.mediaController = null
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), ValueAnimator.AnimatorUpdateListener {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            progressAnimator?.apply {
                cancel()
                progressAnimator = null
            }

            val progress = state?.position?.toInt() ?: 0
            setProgress(progress)

            state?.apply {
                if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                    val timeToEnd =  (max - progress) / state.playbackSpeed

                    progressAnimator = ValueAnimator.ofInt(progress, max).setDuration(timeToEnd.toLong())
                    progressAnimator!!.interpolator = LinearInterpolator()
                    progressAnimator!!.addUpdateListener(this@ControllerCallback)
                    progressAnimator!!.start()
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0
            progress = 0
            setMax(max.toInt())
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            if (isTracking) {
                valueAnimator.cancel()
                return
            }

            progress = valueAnimator.animatedValue as Int
        }

    }

}