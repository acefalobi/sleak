package com.aceinteract.android.sleak.ui.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.util.NetworkUtil
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


abstract class BaseActivity : AppCompatActivity() {

    inline fun <reified T : ViewModel> FragmentActivity.obtainViewModel(viewModelFactory: ViewModelProvider.NewInstanceFactory) =
        android.arch.lifecycle.ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this@BaseActivity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isNetworkConnected(): Boolean {
        return NetworkUtil.isNetworkConnected(applicationContext)
    }

    fun openFragment(fragment: Fragment, fragmentManager: FragmentManager, tag: String) {
        if (fragmentManager.fragments[fragmentManager.backStackEntryCount - 1].tag != tag) {
            fragmentManager.transact {
                setCustomAnimations(
                    R.anim.anim_slide_left_enter,
                    R.anim.anim_slide_left_exit,
                    R.anim.anim_slide_right_enter,
                    R.anim.anim_slide_right_exit
                )
                add(R.id.frame_container, fragment, tag)
                addToBackStack(tag)
            }
        }
    }

    fun replaceFragmentInActivity(fragment: Fragment, frameId: Int) {
        supportFragmentManager.transact {
            replace(frameId, fragment)
        }
    }

    inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
        beginTransaction().apply {
            action()
        }.commit()
    }

}