package com.aceinteract.android.sleak.util

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtil {

    companion object {

        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnectedOrConnecting
        }

    }

}