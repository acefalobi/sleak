package com.aceinteract.android.sleak.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.data.local.dao.SongsDao

@Database(entities = [(Song::class)], version = 1, exportSchema = false)
abstract class SleakDatabase : RoomDatabase() {

    abstract fun songsDao(): SongsDao

    companion object {

        private var INSTANCE: SleakDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): SleakDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        SleakDatabase::class.java, "Sleak.db")
                        .build()
                }
                return INSTANCE!!
            }
        }

    }

}