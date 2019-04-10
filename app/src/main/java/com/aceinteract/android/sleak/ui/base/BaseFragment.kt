package com.aceinteract.android.sleak.ui.base

import android.annotation.TargetApi
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.Fragment

abstract class BaseFragment : Fragment() {

    inline fun <reified T : ViewModel> Fragment.obtainViewModel(viewModelFactory: ViewModelProvider.NewInstanceFactory) =
        android.arch.lifecycle.ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    @TargetApi(Build.VERSION_CODES.M)
    fun hasPermission(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || context!!.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun openFragment(fragment: Fragment, tag: String) {
        val sleakActivity = (activity!! as BaseActivity)
        sleakActivity.openFragment(fragment, sleakActivity.supportFragmentManager, tag)
    }

    fun closeFragment() {
        val sleakActivity = (activity!! as BaseActivity)
        sleakActivity.supportFragmentManager.popBackStack()
    }

}

interface OnBackPressed {

    fun onBackPressed() : Boolean

}