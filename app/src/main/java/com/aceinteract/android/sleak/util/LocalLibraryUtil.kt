package com.aceinteract.android.sleak.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v7.graphics.Palette
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.data.entity.Song
import java.io.File
import java.io.FileNotFoundException


class LocalLibraryUtil(private val context: Context) {

    fun getAllSongs(): ArrayList<Song> {

        val songs = ArrayList<Song>()

        val contentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)


        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR))
                val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                val uriString = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                songs.add(Song(id, title, album, albumId, artist, "", year, duration, uriString))
            }

            cursor.close()
        }

        return songs

    }

    fun removeSong(songId: Long) {
        context.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media._ID} = $songId", null)
    }

    fun getSong(songId: Long): Song? {

        var song: Song? = null

        val contentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media._ID + "=? "
        val selectionArgs = arrayOf(songId.toString())
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)


        if (cursor != null && cursor.count > 0) {
            cursor.moveToPosition(0)

            val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
            val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
            val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            val year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR))
            val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

            val uriString = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

            song = Song(id, title, album, albumId, artist, "", year, duration, uriString)

            cursor.close()
        }

        return song

    }

    fun getSongData(songId: Long): String {

        var data = ""

        val contentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media._ID + "=? "
        val selectionArgs = arrayOf(songId.toString())
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)


        if (cursor != null && cursor.count > 0) {
            cursor.moveToPosition(0)

            data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

            cursor.close()
        }

        return data

    }

    fun getAlbumId(songId: Long): Long {

        var albumId: Long = -1

        val contentResolver = context.contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media._ID + "=? "
        val selectionArgs = arrayOf(songId.toString())
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)


        if (cursor != null && cursor.count > 0) {
            cursor.moveToPosition(0)

            albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))

            cursor.close()
        }

        return albumId

    }

    fun getAlbumArt(albumId: Long): Uri {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(artworkUri, albumId)
    }

    fun getAlbumArtBitmap(songId: Long): Bitmap {
        val albumId = getAlbumId(songId)
        val albumArtUri = getAlbumArt(albumId)
        return getAlbumArtBitmap(albumArtUri)
    }

    fun getAlbumArtBitmap(albumArtUri: Uri): Bitmap = try {
        MediaStore.Images.Media.getBitmap(context.contentResolver, albumArtUri)
    } catch (e: FileNotFoundException) {
        context.getDrawable(R.mipmap.album_art)!!.toBitmap()
    }

    fun getAlbumArtAccent(bitmap: Bitmap): Int {
        val palette = Palette.from(bitmap).generate()
        val dominantColor = palette.getDominantColor(ContextCompat.getColor(context, R.color.color_accent))
        return if (dominantColor.isDark()) {
            dominantColor
        } else {
            val mutedColor = palette.getDarkMutedColor(ContextCompat.getColor(context, R.color.color_accent))
            palette.getDarkVibrantColor(mutedColor)
        }
    }

    fun getAlbumArtAccent(songId: Long): Int = getAlbumArtAccent(getAlbumArtBitmap(songId))


    fun getSongMetadata(songId: Long): MediaMetadataCompat? {
        val song = getSong(songId)
        return if (song != null) getSongMetadata(song)
        else null
    }

    fun getSongMetadata(song: Song): MediaMetadataCompat {

        val albumArt = getAlbumArtBitmap(song.id)

        val builder = MediaMetadataCompat.Builder()
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

        return builder.build()

    }

    fun getMediaItems(songs: List<Song>): List<MediaBrowserCompat.MediaItem> {
        val items = ArrayList<MediaBrowserCompat.MediaItem>()
        songs.forEach {
            items.add(MediaBrowserCompat.MediaItem(createMediaMetadataFromSong(it).description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
        }
        return items
    }

    fun createMediaMetadataFromSong(song: Song): MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.uriString)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.uriString)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
        .build()

    fun deleteSong(path: String): Boolean {
        val fileToDelete = File(path)
        return if (fileToDelete.exists()) fileToDelete.delete()
        else false
    }

    fun deleteSong(songId: Long): Boolean {
        val song = getSong(songId)!!
        return deleteSong(getSongData(songId))
    }

}