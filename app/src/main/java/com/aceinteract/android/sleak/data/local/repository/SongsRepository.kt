package com.aceinteract.android.sleak.data.local.repository

import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.data.source.SongsDataSource

class SongsRepository(private val songsLocalDataSource: SongsDataSource)
    : BaseRepository(), SongsDataSource {

    override fun getSongsByAlbumId(albumId: String, callback: SongsDataSource.LoadSongsCallback) {
        songsLocalDataSource.getSongsByAlbumId(albumId, object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                callback.onSongsLoaded(songs)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun getSongsByArtist(artist: String, callback: SongsDataSource.LoadSongsCallback) {
        songsLocalDataSource.getSongsByArtist(artist, object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                callback.onSongsLoaded(songs)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun getSongsByGenre(genre: String, callback: SongsDataSource.LoadSongsCallback) {
        songsLocalDataSource.getSongsByGenre(genre, object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                callback.onSongsLoaded(songs)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun getSongs(callback: SongsDataSource.LoadSongsCallback) {
        songsLocalDataSource.getSongs(object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                callback.onSongsLoaded(songs)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun getSongById(songId: Long, callback: SongsDataSource.GetSongCallback) {
        songsLocalDataSource.getSongById(songId, object : SongsDataSource.GetSongCallback {
            override fun onSongLoaded(song: Song) {
                callback.onSongLoaded(song)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun saveSong(song: Song) {
        songsLocalDataSource.saveSong(song)
    }

    override fun saveSongs(songs: List<Song>) {
        songsLocalDataSource.saveSongs(songs)
    }

    override fun refreshSongs(songs: ArrayList<Song>, callback: SongsDataSource.LoadSongsCallback) {
        songsLocalDataSource.refreshSongs(songs, object : SongsDataSource.LoadSongsCallback {
            override fun onSongsLoaded(songs: List<Song>) {
                callback.onSongsLoaded(songs)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    override fun deleteAllSongs(callback: SongsDataSource.ModifiedSongCallback?) {
        songsLocalDataSource.deleteAllSongs(callback)
    }

    override fun deleteSong(songId: Long, callback: SongsDataSource.ModifiedSongCallback?) {
        songsLocalDataSource.deleteSong(songId, callback)
    }

    companion object {

        private var INSTANCE: SongsRepository? = null

        @JvmStatic
        fun getInstance(songsLocalDataSource: SongsDataSource) = INSTANCE ?: synchronized(SongsRepository::class.java) {
                INSTANCE ?: SongsRepository(songsLocalDataSource)
                    .also { INSTANCE = it }
            }


        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }


}