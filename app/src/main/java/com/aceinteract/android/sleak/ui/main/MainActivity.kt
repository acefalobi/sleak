package com.aceinteract.android.sleak.ui.main

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.ContextMenu
import android.view.View
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.databinding.ActivityMainBinding
import com.aceinteract.android.sleak.ui.base.BaseActivity
import com.aceinteract.android.sleak.ui.base.OnBackPressed
import com.aceinteract.android.sleak.ui.main.home.HomeFragment
import com.aceinteract.android.sleak.ui.nowplaying.NowPlayingActivity
import com.aceinteract.android.sleak.util.StorageUtil


class MainActivity : BaseActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private var mediaBrowserListener = MediaBrowserListener()

    private lateinit var fragmentManager: FragmentManager

    private val listener = object : MainItemActionListener {
        override fun onPlayPauseClicked(isPlaying: Boolean) {
            if (isPlaying)
                SleakApplication.INSTANCE?.getTransportControls()?.pause()
            else
                SleakApplication.INSTANCE?.getTransportControls()?.play()
        }

        override fun onNowPlayingClicked() {
            startActivity(Intent(this@MainActivity, NowPlayingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater, null, false).apply {
            viewModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
            viewModel!!.start()
            listener = this@MainActivity.listener
        }

        setContentView(activityMainBinding.root)

        setupFragments()
    }

    override fun onStart() {
        super.onStart()
        activityMainBinding.pbMiniPlayer.setMediaController(SleakApplication.INSTANCE?.mediaBrowserHelper?.mediaController!!)
        SleakApplication.INSTANCE?.mediaBrowserHelper?.registerCallback(mediaBrowserListener)
    }

    override fun onStop() {
        super.onStop()
        activityMainBinding.pbMiniPlayer.disconnectController()
        SleakApplication.INSTANCE?.mediaBrowserHelper?.unregisterCallback(mediaBrowserListener)
    }

    @SuppressLint("CommitTransaction")
    private fun setupFragments() {

        fragmentManager = supportFragmentManager
        fragmentManager.addOnBackStackChangedListener {
            if (fragmentManager.backStackEntryCount < 1) {
                finish()
            }
        }

        fragmentManager.transact {
            replace(R.id.frame_container, HomeFragment(), HomeFragment.TAG)
            addToBackStack(HomeFragment.TAG)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(HomeFragment.TAG)
        if (fragment !is OnBackPressed || !(fragment as OnBackPressed).onBackPressed()) {
            super.onBackPressed()
        }
    }

    private inner class MediaBrowserListener : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(playbackState)
            activityMainBinding.viewModel?.run {
                isPlaying.set(playbackState != null && playbackState.state == PlaybackStateCompat.STATE_PLAYING)
            }
        }

        override fun onMetadataChanged(mediaMetadata: MediaMetadataCompat?) {
            super.onMetadataChanged(mediaMetadata)
            if (mediaMetadata == null) {
                return
            }

            activityMainBinding.viewModel?.run {
                playingSong.set(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong())
                loadSong()
            }
        }

    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_track, menu)
    }
}
