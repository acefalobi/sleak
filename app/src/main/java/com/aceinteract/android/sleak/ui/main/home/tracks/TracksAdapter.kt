package com.aceinteract.android.sleak.ui.main.home.tracks

import android.content.Context
import android.databinding.Observable
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.data.entity.Song
import com.aceinteract.android.sleak.databinding.ItemTrackBinding
import com.aceinteract.android.sleak.ui.main.MainActivity
import com.aceinteract.android.sleak.util.SongDiffCallback
import com.aceinteract.android.sleak.util.LocalLibraryUtil


class TracksAdapter(private val context: Context, private var songs: List<Song>, private val tracksViewModel: TracksViewModel)
    : RecyclerView.Adapter<TracksAdapter.TrackViewHolder>() {

    private lateinit var binding: ItemTrackBinding

    fun updateListItems(songs: ArrayList<Song>) {
        with (this.songs as ArrayList) {
            val diffCallback = SongDiffCallback(this, songs)
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            clear()
            addAll(songs)
            diffResult.dispatchUpdatesTo(this@TracksAdapter)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): TrackViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        binding = ItemTrackBinding.inflate(inflater, p0, false)
        return TrackViewHolder(binding.root)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(viewHolder: TrackViewHolder, position: Int) {
        val itemActionListener = object : SongItemActionListener {
            override fun onSongClicked(song: Song) {
                tracksViewModel.playSongEvent.value = viewHolder.adapterPosition
            }

            override fun onSongLongClicked(song: Song): Boolean {
                tracksViewModel.positionSelected.value = viewHolder.adapterPosition
                return false
            }
        }

//        (context as MainActivity).registerForContextMenu(binding.root)

        with(binding) {
            song = songs[viewHolder.adapterPosition]
            listener = itemActionListener

            if (song!!.id == tracksViewModel.playingSong.get()) {
                textSongTitle.setTextColor(LocalLibraryUtil(context).getAlbumArtAccent(song!!.id))
                textSongTitle.typeface = Typeface.createFromAsset(context.assets, "fonts/Montserrat-SemiBold.ttf")
            }

            tracksViewModel.playingSong.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {

                    if (song!!.id == tracksViewModel.playingSong.get()) {
                        textSongTitle.setTextColor(LocalLibraryUtil(context).getAlbumArtAccent(song!!.id))
                        textSongTitle.typeface = Typeface.createFromAsset(context.assets, "fonts/Montserrat-SemiBold.ttf")
                    } else {
                        textSongTitle.setTextColor(ContextCompat.getColor(context, R.color.dark_bright))
                        textSongTitle.typeface = Typeface.createFromAsset(context.assets, "fonts/Montserrat-Medium.ttf")
                    }
                }
            })

            executePendingBindings()
        }
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
