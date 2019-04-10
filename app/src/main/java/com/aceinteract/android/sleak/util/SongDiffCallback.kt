package com.aceinteract.android.sleak.util

import android.support.v7.util.DiffUtil
import com.aceinteract.android.sleak.data.entity.Song

class SongDiffCallback(private val oldList: List<Song>, private val newList: List<Song>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        areItemsTheSame(oldItemPosition, newItemPosition)

}