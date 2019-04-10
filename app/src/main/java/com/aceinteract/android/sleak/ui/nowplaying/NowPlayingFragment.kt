package com.aceinteract.android.sleak.ui.nowplaying

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.databinding.NowPlayingFragmentBinding
import com.aceinteract.android.sleak.ui.base.BaseActivity

class NowPlayingFragment : Fragment() {

    private lateinit var dataBinding: NowPlayingFragmentBinding

    private var mediaBrowserListener = MediaBrowserListener()

    private val listener = object : NowPlayingItemActionListener {
        override fun onPlayPauseClicked(isPlaying: Boolean) {
            if (isPlaying)
                SleakApplication.INSTANCE?.getTransportControls()?.pause()
            else
                SleakApplication.INSTANCE?.getTransportControls()?.play()
        }

        override fun onNextClicked() {
            SleakApplication.INSTANCE?.getTransportControls()?.skipToNext()
        }

        override fun onPrevClicked() {
            SleakApplication.INSTANCE?.getTransportControls()?.skipToPrevious()
        }

        override fun onQueueClicked() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dataBinding = NowPlayingFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = ViewModelProviders.of(this@NowPlayingFragment).get(NowPlayingViewModel::class.java)
            viewModel!!.start()
            listener = this@NowPlayingFragment.listener
        }

        return dataBinding.root
    }

    override fun onStart() {
        super.onStart()
//        dataBinding.seeker.setMediaController(SleakApplication.INSTANCE?.mediaBrowserHelper?.mediaController!!)
        SleakApplication.INSTANCE?.mediaBrowserHelper?.registerCallback(mediaBrowserListener)
    }

    override fun onStop() {
        super.onStop()
//        dataBinding.seeker.disconnectController()
        SleakApplication.INSTANCE?.mediaBrowserHelper?.unregisterCallback(mediaBrowserListener)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        (activity as BaseActivity).setSupportActionBar(dataBinding.toolbar)
        (activity as BaseActivity).supportActionBar?.title = ""
        (activity as BaseActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as BaseActivity).supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_down)
    }

    private inner class MediaBrowserListener : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(playbackState)
            dataBinding.viewModel?.run {
                isPlaying.set(playbackState != null && playbackState.state == PlaybackStateCompat.STATE_PLAYING)
            }
        }

        override fun onMetadataChanged(mediaMetadata: MediaMetadataCompat?) {
            super.onMetadataChanged(mediaMetadata)
            if (mediaMetadata == null) {
                return
            }

            dataBinding.viewModel?.run {
                playingSong.set(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong())
                loadSong()
            }
        }

    }

}
