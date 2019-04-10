package com.aceinteract.android.sleak.ui.main.home.tracks


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.databinding.FragmentTracksBinding
import com.aceinteract.android.sleak.databinding.FragmentTracksMenuBinding
import com.aceinteract.android.sleak.ui.base.BaseFragment
import com.aceinteract.android.sleak.ui.main.MainActivity
import com.aceinteract.android.sleak.util.LocalLibraryUtil
import com.aceinteract.android.sleak.util.StorageUtil
import com.aceinteract.android.sleak.util.setupSnackbar
import com.aceinteract.android.sleak.view.RoundedBottomSheetDialogFragment


class TracksFragment : BaseFragment() {

    private var overallYScroll = 0

    private val localLibraryUtil: LocalLibraryUtil by lazy { LocalLibraryUtil(context!!) }
    private val storageUtil: StorageUtil by lazy { StorageUtil(context!!) }

    private var position = -1

    private val mediaBrowserListener = MediaBrowserListener()

    private lateinit var dataBinding: FragmentTracksBinding

    private lateinit var tracksAdapter: TracksAdapter

    override fun onStop() {
        super.onStop()
        SleakApplication.INSTANCE?.mediaBrowserHelper?.unregisterCallback(mediaBrowserListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = FragmentTracksBinding.inflate(inflater, container, false).apply {
            viewModel = obtainViewModel(TrackViewModelFactory.getInstance(activity!!.application as SleakApplication))
            viewModel!!.start()

            rvSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    overallYScroll += dy
                    if (overallYScroll != 0) (activity as MainActivity).findViewById<AppBarLayout>(R.id.appbar).elevation = 8f
                    else (activity as MainActivity).findViewById<AppBarLayout>(R.id.appbar).elevation = 0f
                }
            })
        }

        SleakApplication.INSTANCE?.mediaBrowserHelper?.registerCallback(mediaBrowserListener)

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dataBinding.viewModel?.let {
            view?.setupSnackbar(this, it.snackbarMessage, Snackbar.LENGTH_LONG)
            val queue = storageUtil.queue
            if (queue.isNotEmpty()) {
                it.playingSong.set(queue[storageUtil.currentQueuePosition].toLong())
            }
        }
        setupAdapter()
        setupEventObservers()
    }

    private fun setupEventObservers() {

        dataBinding.viewModel!!.apply {
            playSongEvent.observe(this@TracksFragment, Observer {
                it?.let { pos ->
                    loadNewPlaylist(tracks, pos)
                }
            })
            positionSelected.observe(this@TracksFragment, Observer {
                it?.let { pos ->
                    position = pos
                    val menuFragment = TrackMenuFragment()
                    menuFragment.show(activity!!.supportFragmentManager, menuFragment.tag)
//                    val popupMenu = PopupMenu(context!!, dataBinding.root, Gravity.BOTTOM)
//                    popupMenu.menuInflater.inflate(R.menu.menu_track, popupMenu.menu)
//                    popupMenu.setOnMenuItemClickListener { item ->
//                        val track = dataBinding.viewModel!!.tracks[position]
//                        when (item.itemId) {
//                            R.id.action_play_next -> {
//                                val queue = storageUtil.queue
//                                queue.add(storageUtil.currentQueuePosition + 1, track.id.toString())
//                                storageUtil.queue = queue
//                                dataBinding.viewModel!!.showSnackbarMessage(R.string.info_playing_next)
//                                true
//                            }
//                            R.id.action_add_to_queue -> {
//                                val queue = storageUtil.queue
//                                queue.add(track.id.toString())
//                                storageUtil.queue = queue
//                                dataBinding.viewModel!!.showSnackbarMessage(R.string.info_added_to_queue)
//                                true
//                            }
//                            R.id.action_delete -> {
//                                AlertDialog.Builder(context!!)
//                                    .setTitle(R.string.title_delete_track)
//                                    .setMessage(R.string.info_delete_track)
//                                    .setPositiveButton(R.string.desc_positive_button) { _, _ ->
//                                        dataBinding.viewModel!!.deleteSong(track)
//                                        dataBinding.viewModel!!.showSnackbarMessage(R.string.info_deleted_song)
////                        if (localLibraryUtil.deleteSong(localLibraryUtil.getSongData(track.id))) {
////                            dataBinding.viewModel!!.deleteSong(track)
////                            dataBinding.viewModel!!.showSnackbarMessage(R.string.info_deleted_song)
////                        } else {
////                            dataBinding.viewModel!!.showSnackbarMessage(R.string.error_delete_file)
////                        }
//                                    }
//                                    .setNegativeButton(R.string.desc_negative_button) { _, _ ->
//                                        return@setNegativeButton
//                                    }.create().show()
//                                true
//                            }
//                            else -> super.onContextItemSelected(item)
//                        }
//                    }
//                    popupMenu.show()
                }
            })
        }
    }

    private fun setupAdapter() {
        val viewModel = dataBinding.viewModel
        viewModel?.apply {
            tracksAdapter = TracksAdapter(context!!, ArrayList(0), this)
            dataBinding.rvSongs.adapter = tracksAdapter
            dataBinding.rvSongs.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val track = dataBinding.viewModel!!.tracks[position]
        return when (item.itemId) {
            R.id.action_play_next -> {
                val queue = storageUtil.queue
                queue.add(storageUtil.currentQueuePosition + 1, track.id.toString())
                storageUtil.queue = queue
                dataBinding.viewModel!!.showSnackbarMessage(R.string.info_playing_next)
                true
            }
            R.id.action_add_to_queue -> {
                val queue = storageUtil.queue
                queue.add(track.id.toString())
                storageUtil.queue = queue
                dataBinding.viewModel!!.showSnackbarMessage(R.string.info_added_to_queue)
                true
            }
            R.id.action_delete -> {
                AlertDialog.Builder(context!!)
                    .setTitle(R.string.title_delete_track)
                    .setMessage(R.string.info_delete_track)
                    .setPositiveButton(R.string.desc_positive_button) { _, _ ->
                        dataBinding.viewModel!!.deleteSong(track)
                        dataBinding.viewModel!!.showSnackbarMessage(R.string.info_deleted_song)
//                        if (localLibraryUtil.deleteSong(localLibraryUtil.getSongData(track.id))) {
//                            dataBinding.viewModel!!.deleteSong(track)
//                            dataBinding.viewModel!!.showSnackbarMessage(R.string.info_deleted_song)
//                        } else {
//                            dataBinding.viewModel!!.showSnackbarMessage(R.string.error_delete_file)
//                        }
                    }
                    .setNegativeButton(R.string.desc_negative_button) { _, _ ->
                        return@setNegativeButton
                    }.create().show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private inner class MediaBrowserListener : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(mediaMetadata: MediaMetadataCompat?) {
            super.onMetadataChanged(mediaMetadata)
            if (mediaMetadata == null) {
                return
            }
            dataBinding.viewModel?.playingSong?.set(mediaMetadata.description.mediaId!!.toLong())
        }

    }

    class TrackMenuFragment : RoundedBottomSheetDialogFragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return FragmentTracksMenuBinding.inflate(inflater, container, false).root
        }

    }

}
