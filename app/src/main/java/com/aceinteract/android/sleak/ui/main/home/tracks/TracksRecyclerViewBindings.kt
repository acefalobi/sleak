package com.aceinteract.android.sleak.ui.main.home.tracks

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.aceinteract.android.sleak.data.entity.Song

object TracksRecyclerViewBindings {

    @BindingAdapter("app:items")
    @JvmStatic
    fun RecyclerView.setItems(items: ArrayList<Song>?) {
        if (items != null) {
            with(adapter as TracksAdapter) {
                updateListItems(items)
            }
        }
    }

}