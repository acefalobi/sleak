package com.aceinteract.android.sleak.data.local.source

import android.support.annotation.VisibleForTesting
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.data.local.dao.SongsDao
import com.aceinteract.android.sleak.data.source.SongsDataSource
import com.aceinteract.android.sleak.util.AppExecutors

class SongsLocalDataSource private constructor(private val appExecutors: AppExecutors, private val songsDao: SongsDao)
    : SongsDataSource {

    override fun getSongsByAlbumId(albumId: String, callback: SongsDataSource.LoadSongsCallback) {
        appExecutors.diskIO.execute {
            val songs = songsDao.getSongsByAlbumId(albumId)
            appExecutors.mainThread.execute {
                if (songs.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    callback.onSongsLoaded(songs as ArrayList<Song>)
                }
            }
        }
    }

    override fun getSongsByArtist(artist: String, callback: SongsDataSource.LoadSongsCallback) {
        appExecutors.diskIO.execute {
            val songs = songsDao.getSongsByArtist(artist)
            appExecutors.mainThread.execute {
                if (songs.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    callback.onSongsLoaded(songs as ArrayList<Song>)
                }
            }
        }
    }

    override fun getSongsByGenre(genre: String, callback: SongsDataSource.LoadSongsCallback) {
        appExecutors.diskIO.execute {
            val songs = songsDao.getSongsByGenre(genre)
            appExecutors.mainThread.execute {
                if (songs.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    callback.onSongsLoaded(songs as ArrayList<Song>)
                }
            }
        }
    }

    override fun getSongById(songId: Long, callback: SongsDataSource.GetSongCallback) {
        appExecutors.diskIO.execute {
            val song = songsDao.getSongById(songId)
            appExecutors.mainThread.execute {
                if (song != null) {
                    callback.onSongLoaded(song)
                } else {
                    callback.onDataNotAvailable()
                }
            }
        }
    }

    override fun getSongs(callback: SongsDataSource.LoadSongsCallback) {
        appExecutors.diskIO.execute {
            val songs = songsDao.getSongs()
            appExecutors.mainThread.execute {
                if (songs.isEmpty()) {
                    callback.onDataNotAvailable()
                } else {
                    callback.onSongsLoaded(songs as ArrayList<Song>)
                }
            }
        }
    }

    override fun saveSong(song: Song) {
        appExecutors.diskIO.execute { songsDao.insertSong(song) }
    }
    override fun saveSongs(songs: List<Song>) {
        appExecutors.diskIO.execute { songsDao.insertSongs(songs) }
    }

    override fun refreshSongs(songs: ArrayList<Song>, callback: SongsDataSource.LoadSongsCallback) {

        appExecutors.diskIO.execute {

            songs.forEach {
                val currentSong = songsDao.getSongByUri(it.uriString)
                if (currentSong == null) {
                    songsDao.insertSong(it)
                } else {
                    it.id = currentSong.id
                    songsDao.updateSong(it)
                }
            }

            val updatedSongs = songsDao.getSongs()
            if (updatedSongs.isEmpty()) {
                callback.onDataNotAvailable()
            } else {
                callback.onSongsLoaded(updatedSongs as ArrayList<Song>)
            }

        }

    }

    override fun deleteAllSongs(callback: SongsDataSource.ModifiedSongCallback?) {
        appExecutors.diskIO.execute {
            songsDao.deleteAllSongs()
            appExecutors.mainThread.execute {
                callback?.onModified()
            }
        }
    }

    override fun deleteSong(songId: Long, callback: SongsDataSource.ModifiedSongCallback?) {
        appExecutors.diskIO.execute {
            songsDao.deleteSongById(songId)
            appExecutors.mainThread.execute {
                callback?.onModified()
            }
        }
    }

    companion object {
        private var INSTANCE: SongsLocalDataSource? = null

        @JvmStatic
        fun getInstance(appExecutors: AppExecutors, songsDao: SongsDao): SongsLocalDataSource {
            if (INSTANCE == null) {
                synchronized(SongsLocalDataSource::javaClass) {
                    INSTANCE = SongsLocalDataSource(appExecutors, songsDao)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }

}