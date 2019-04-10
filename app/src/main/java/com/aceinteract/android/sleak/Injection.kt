package com.aceinteract.android.sleak

import android.content.Context
import com.aceinteract.android.sleak.data.local.SleakDatabase
import com.aceinteract.android.sleak.data.local.repository.SongsRepository
import com.aceinteract.android.sleak.data.local.source.SongsLocalDataSource
import com.aceinteract.android.sleak.util.AppExecutors

object Injection {

    fun provideSongsRepository(context: Context): SongsRepository {
        val database = SleakDatabase.getInstance(context)
        return SongsRepository.getInstance(SongsLocalDataSource.getInstance(AppExecutors(), database.songsDao()))
    }

}