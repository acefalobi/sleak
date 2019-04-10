package com.aceinteract.android.sleak.ui.nowplaying

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.MenuItem
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.ui.base.BaseActivity
import com.aceinteract.android.sleak.ui.main.MainActivity

class NowPlayingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.now_playing_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NowPlayingFragment())
                .commitNow()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(mainIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
