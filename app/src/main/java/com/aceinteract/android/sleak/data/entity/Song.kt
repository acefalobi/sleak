package com.aceinteract.android.sleak.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.ContentUris
import android.net.Uri


@Entity(tableName = "songs")
data class Song constructor(

    @PrimaryKey var id: Long,

    var title: String,
    var album: String,
    var albumId: Long,
    var artist: String,
    var genre: String,
    var year: String,
    var duration: Long,
    var uriString: String

) : BaseEntity() {

    val trackUri: Uri
        get() = Uri.parse(uriString)

    val albumArtUri: Uri
        get() {
            val artworkUri = Uri.parse("content://media/external/audio/albumart")

            return ContentUris.withAppendedId(artworkUri, albumId)
        }

    fun getLengthInMinutes(): String {
        val inSeconds = duration / 1000.0
        val minutes = (inSeconds / 60.0).toInt()
        val seconds = (inSeconds % 60).toInt()
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

}