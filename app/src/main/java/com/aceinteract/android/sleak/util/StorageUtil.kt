package com.aceinteract.android.sleak.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StorageUtil(context: Context) {

    private var preferences: SharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)

    var queue: ArrayList<String> = ArrayList()
        get() {
            val json = preferences.getString(QUEUE, null)
            return if (json != null) {
                val type = object : TypeToken<ArrayList<String>>() {}.type
                Gson().fromJson(json, type)
            } else ArrayList()
        }
        @SuppressLint("ApplySharedPref")
        set(value) {
            val previousJson = Gson().toJson(field)
            val json = Gson().toJson(value)
            if (previousJson != json) {
                val editor = preferences.edit()
                editor.putString(QUEUE, json)
                editor.commit()
            }

            currentQueuePosition = 0
        }


    var currentQueuePosition: Int
        get() = preferences.getInt(QUEUE_POSITION, 0)
        @SuppressLint("ApplySharedPref")
        set(value) {
            val editor = preferences.edit()
            editor.putInt(QUEUE_POSITION, value)
            editor.commit()
        }

    fun clearStorage() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val STORAGE = "com.aceinteract.sleak.util.STORAGE"

        private const val QUEUE = "queue"
        private const val QUEUE_POSITION = "queue_position"
    }

}