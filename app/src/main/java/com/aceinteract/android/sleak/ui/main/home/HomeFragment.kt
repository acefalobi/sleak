package com.aceinteract.android.sleak.ui.main.home


import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.bottomappbar.BottomAppBar
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.SleakApplication
import com.aceinteract.android.sleak.ui.base.BaseActivity
import com.aceinteract.android.sleak.ui.base.BaseFragment
import com.aceinteract.android.sleak.ui.base.OnBackPressed
import com.aceinteract.android.sleak.util.LocalLibraryUtil
import com.aceinteract.android.sleak.util.StorageUtil

class HomeFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener, OnBackPressed {

    private lateinit var localLibraryUtil: LocalLibraryUtil
    private lateinit var storageUtil: StorageUtil

    private var accentColor: Int = -1

    private var tabListener = object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            tab?.icon?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> tab.setIcon(R.drawable.ic_outline_home)
                1 -> tab.setIcon(R.drawable.ic_radio_button_checked)
                2 -> tab.setIcon(R.drawable.ic_outline_music_note)
                3 -> tab.setIcon(R.drawable.outline_queue_music_black_48)
                4 -> tab.setIcon(R.drawable.ic_person_outline)
            }
            tab?.icon?.setColorFilter(ContextCompat.getColor(context!!, R.color.dark_dim), PorterDuff.Mode.SRC_IN)
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> tab.setIcon(R.drawable.ic_home)
                1 -> tab.setIcon(R.drawable.ic_radio_button_checked)
                2 -> tab.setIcon(R.drawable.ic_music_note)
                3 -> tab.setIcon(R.drawable.ic_queue_music)
                4 -> tab.setIcon(R.drawable.ic_person_black_24dp)
            }
            tab?.icon?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        }

    }

    private var mediaBrowserListener = MediaBrowserListener()

    private lateinit var pagerAdapter: ViewPagerAdapter

    private lateinit var pager: ViewPager

    private var tabLayout: TabLayout? = null

    private lateinit var drawerLayout: DrawerLayout


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_library -> {
                // Handle the camera action
            }
            R.id.nav_listen_to_lyrics -> {

            }
            R.id.nav_settings -> {

            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }

    override fun onStart() {
        super.onStart()
        SleakApplication.INSTANCE?.mediaBrowserHelper?.registerCallback(mediaBrowserListener)
    }

    override fun onStop() {
        super.onStop()
        SleakApplication.INSTANCE?.mediaBrowserHelper?.unregisterCallback(mediaBrowserListener)
    }

    override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, viewGroup, false)

        localLibraryUtil = LocalLibraryUtil(context!!)
        storageUtil = StorageUtil(context!!)

        (activity as BaseActivity).setSupportActionBar(view.findViewById(R.id.toolbar))
        (activity as BaseActivity).supportActionBar?.title = ""

        setHasOptionsMenu(true)

        drawerLayout = view.findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(activity, drawerLayout, view.findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        view.findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)

        pagerAdapter =
            ViewPagerAdapter((activity as BaseActivity).supportFragmentManager)

        tabLayout = view.findViewById(R.id.tabs)

        tabLayout?.addOnTabSelectedListener(tabListener)

        pager = view.findViewById(R.id.container)
        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        accentColor = ContextCompat.getColor(context!!, R.color.color_accent)

        tabLayout!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))

        val queue = storageUtil.queue
        if (queue.isNotEmpty()) {

            accentColor = localLibraryUtil.getAlbumArtAccent(queue[storageUtil.currentQueuePosition].toLong())

        }

        tabLayout?.getTabAt(2)?.select()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onBackPressed(): Boolean {
        return if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        } else false
    }

    companion object {
        const val TAG = "HomeFragment"
    }

    private inner class MediaBrowserListener : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(mediaMetadata: MediaMetadataCompat?) {
            super.onMetadataChanged(mediaMetadata)
            if (mediaMetadata == null) {
                return
            }
            accentColor = localLibraryUtil.getAlbumArtAccent(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toLong()
            )
            tabLayout?.getTabAt(tabLayout!!.selectedTabPosition)?.icon?.setColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        }

    }

}
