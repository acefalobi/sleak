package com.aceinteract.android.sleak.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import com.aceinteract.android.sleak.Injection
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.ui.base.BaseActivity
import com.aceinteract.android.sleak.ui.main.MainActivity
import com.aceinteract.android.sleak.util.LocalLibraryUtil

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            scheduleSplashScreen()
        } else {
            ActivityCompat.requestPermissions(this@SplashActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                SleakApplication.PERMISSION_WRITE_READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SleakApplication.PERMISSION_WRITE_READ_EXTERNAL_STORAGE -> {
                if (grantResults.size == 2) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        scheduleSplashScreen()
                    } else {
                        ActivityCompat.requestPermissions(this@SplashActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
                    }
                } else
                    ActivityCompat.requestPermissions(this@SplashActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            }
        }
    }

    private fun scheduleSplashScreen() {

        val songs = LocalLibraryUtil(this).getAllSongs()

        Injection.provideSongsRepository(applicationContext).deleteAllSongs(null)
        Injection.provideSongsRepository(applicationContext).saveSongs(songs)

        SleakApplication.INSTANCE?.startMediaBrowserConnection()

        Handler().postDelayed({
            routeToActivity()
            finish()
        }, 2000L)
    }

    private fun routeToActivity() = startActivity(Intent(this@SplashActivity, MainActivity::class.java))

}
