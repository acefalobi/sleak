package com.aceinteract.android.sleak.data.source

import com.aceinteract.android.sleak.data.entity.Song

interface SongsDataSource {

    interface LoadSongsCallback : DataSourceCallback {

        fun onSongsLoaded(songs: List<Song>)

    }

    interface GetSongCallback : DataSourceCallback {

        fun onSongLoaded(song: Song)

    }

    interface ModifiedSongCallback {

        fun onModified()

    }

    fun getSongs(callback: LoadSongsCallback)

    fun getSongsByAlbumId(albumId: String, callback: LoadSongsCallback)

    fun getSongsByArtist(artist: String, callback: LoadSongsCallback)

    fun getSongsByGenre(genre: String, callback: LoadSongsCallback)

    fun getSongById(songId: Long, callback: GetSongCallback)

    fun saveSong(song: Song)

    fun saveSongs(songs: List<Song>)

    fun refreshSongs(songs: ArrayList<Song>, callback: LoadSongsCallback)

    fun deleteAllSongs(callback: ModifiedSongCallback?)

    fun deleteSong(songId: Long, callback: ModifiedSongCallback?)

}

interface DataSourceCallback {

    fun onDataNotAvailable()

}