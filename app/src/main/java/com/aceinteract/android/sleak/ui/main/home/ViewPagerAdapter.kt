package com.aceinteract.android.sleak.ui.main.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.aceinteract.android.sleak.ui.main.home.start.StartFragment
import com.aceinteract.android.sleak.ui.main.home.tracks.TracksFragment

class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            2 -> TracksFragment()
            else -> StartFragment()
        }
    }

    override fun getCount(): Int {
        return 5
    }

}