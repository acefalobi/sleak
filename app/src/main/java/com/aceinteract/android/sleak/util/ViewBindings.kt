package com.aceinteract.android.sleak.util

import android.content.res.ColorStateList
import android.databinding.BindingAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("app:drawableSrc")
fun FloatingActionButton.setDrawableImage(drawable: Drawable) = setImageDrawable(drawable)

@BindingAdapter("app:adaptiveBackgroundColor")
fun FloatingActionButton.setAdaptiveBackgroundColor(songId: Long) {
    backgroundTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
}

@BindingAdapter("app:drawableSrc")
fun AppCompatImageButton.setDrawableImage(drawable: Drawable) = setImageDrawable(drawable)

@BindingAdapter("app:adaptiveTextColor")
fun TextView.setAdaptiveTextColor(songId: Long) = setTextColor(LocalLibraryUtil(context).getAlbumArtAccent(songId))

@BindingAdapter("app:adaptiveProgressColor")
fun ProgressBar.setAdaptiveProgressColor(songId: Long) {
    progressTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
    progressBackgroundTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
}

@BindingAdapter("app:adaptiveSeekerColor")
fun SeekBar.setAdaptiveSeekerColor(songId: Long) {
    progressTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
    progressBackgroundTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
    thumbTintList = ColorStateList.valueOf(LocalLibraryUtil(context).getAlbumArtAccent(songId))
}

@BindingAdapter("app:adaptiveImage")
fun AppCompatImageView.setAdaptiveTextColor(songId: Long) {
    setImageDrawable(ColorDrawable(LocalLibraryUtil(context).getAlbumArtAccent(songId)))
}

@BindingAdapter("app:imageUrl")
fun ImageView.loadImageFromUrl(imageUrl: String) {

    Glide.with(context).asBitmap()
            .load(imageUrl)
            .thumbnail(.4f)
            .apply(RequestOptions().placeholder(android.R.color.darker_gray))
            .into(this)

}

@BindingAdapter("app:albumArtUri")
fun ImageView.loadAlbumArtFromUri(imageUri: Uri) {
    val appExecutors = AppExecutors()
    appExecutors.diskIO.execute {
        val bitmap = LocalLibraryUtil(context).getAlbumArtBitmap(imageUri)
        appExecutors.mainThread.execute { setImageBitmap(bitmap) }
    }
}

@BindingAdapter("app:blurredAlbumArtUri")
fun ImageView.loadBlurredAlbumArtFromUri(imageUri: Uri) {
    val appExecutors = AppExecutors()
    appExecutors.diskIO.execute {
        val bitmap = LocalLibraryUtil(context).getAlbumArtBitmap(imageUri)
        appExecutors.mainThread.execute { setImageBitmap(bitmap) }
    }
}

@BindingAdapter("app:imageUri")
fun ImageView.loadImageFromUri(imageUri: Uri) {

    Glide.with(context).asBitmap()
        .load(imageUri)
        .thumbnail(.4f)
        .apply(RequestOptions().placeholder(android.R.color.darker_gray))
        .into(this)

}