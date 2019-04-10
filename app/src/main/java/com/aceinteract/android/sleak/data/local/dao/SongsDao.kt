package com.aceinteract.android.sleak.data.local.dao

import android.arch.persistence.room.*
import com.aceinteract.android.sleak.data.entity.Song

@Dao
interface SongsDao {

    @Query("SELECT * FROM Songs ORDER BY title")
    fun getSongs(): List<Song>

    @Query("SELECT * FROM Songs WHERE albumId = :albumId")
    fun getSongsByAlbumId(albumId: String): List<Song>

    @Query("SELECT * FROM Songs WHERE artist = :artist")
    fun getSongsByArtist(artist: String): List<Song>

    @Query("SELECT * FROM Songs WHERE genre = :genre")
    fun getSongsByGenre(genre: String): List<Song>

    @Query("SELECT * FROM Songs WHERE id = :songId")
    fun getSongById(songId: Long): Song?

    @Query("SELECT * FROM Songs WHERE uriString = :uriString")
    fun getSongByUri(uriString: String): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongs(song: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: Song)

    @Update
    fun updateSong(song: Song): Int

    @Query("DELETE FROM Songs WHERE id = :songId")
    fun deleteSongById(songId: Long): Int

    @Query("DELETE FROM Songs")
    fun deleteAllSongs()

}