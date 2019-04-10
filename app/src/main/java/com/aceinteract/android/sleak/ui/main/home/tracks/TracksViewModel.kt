package com.aceinteract.android.sleak.ui.main.home.tracks

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.support.annotation.VisibleForTesting
import com.aceinteract.android.sleak.Injection
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.data.local.repository.SongsRepository
import com.aceinteract.android.sleak.data.source.SongsDataSource
import com.aceinteract.android.sleak.ui.base.BaseViewModel
import com.aceinteract.android.sleak.util.SingleLiveEvent
import com.aceinteract.android.sleak.util.StorageUtil

class TracksViewModel(application: Application, private val songsRepository: SongsRepository) : BaseViewModel(application) {

    private val hasTracksLoadingError = ObservableBoolean(false)

    internal val playSongEvent = SingleLiveEvent<Int>()

    val positionSelected = SingleLiveEvent<Int>()

    val tracks: ObservableArrayList<Song> = ObservableArrayList()

    val isTracksLoading = ObservableBoolean(false)
    val tracksEmpty = ObservableBoolean(false)

    fun start() {
        loadSongs()
    }

    fun loadSongs(showLoadingUI: Boolean = false) {
        if (showLoadingUI) {
            isTracksLoading.set(true)
        }

        songsRepository.getSongs(object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                if (showLoadingUI) isTracksLoading.set(false)

                hasTracksLoadingError.set(false)

                with(this@TracksViewModel.tracks) {
                    clear()
                    addAll(songs.sortedBy { it.title })
                    tracksEmpty.set(isEmpty())
                }
                val storageUtil = StorageUtil(context)
                val queue = storageUtil.queue
                if (queue.isNotEmpty()) { playingSong.set(queue[storageUtil.currentQueuePosition].toLong()) }
            }

            override fun onDataNotAvailable() {
                if (showLoadingUI) isTracksLoading.set(false)
                hasTracksLoadingError.set(true)
                showSnackbarMessage(R.string.error_loading_data)
            }
        })
    }

    fun deleteSong(song: Song) {
        songsRepository.deleteSong(song.id, object : SongsDataSource.ModifiedSongCallback {
            override fun onModified() {
                localLibraryUtil.removeSong(song.id)
                val queue = storageUtil.queue
                queue.remove(song.id.toString())
                storageUtil.queue = queue

                val songs = localLibraryUtil.getAllSongs()
                Injection.provideSongsRepository(context).saveSongs(songs)

                loadSongs()
            }
        })
    }

}

@Suppress("UNCHECKED_CAST")
class TrackViewModelFactory private constructor(private val context: Application,
                                                private val songsRepository: SongsRepository)
    : ViewModelProvider.NewInstanceFactory()  {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        with(modelClass) {
            if (isAssignableFrom(TracksViewModel::class.java)) TracksViewModel(context, songsRepository)
            else throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T

    companion object {

        @Volatile
        var INSTANCE: TrackViewModelFactory? = null

        fun getInstance(application: SleakApplication) =
            INSTANCE ?: synchronized(TrackViewModelFactory::class.java) {
                INSTANCE ?: TrackViewModelFactory(application,
                    Injection.provideSongsRepository(application.applicationContext))
                    .also { INSTANCE = it }
            }

        @VisibleForTesting
        fun destroyInstance() {
            INSTANCE = null
        }

    }

}