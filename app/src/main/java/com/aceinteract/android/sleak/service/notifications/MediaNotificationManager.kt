package com.aceinteract.android.sleak.service.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.aceinteract.android.sleak.R
import com.aceinteract.android.sleak.service.MusicService
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.aceinteract.android.sleak.ui.main.MainActivity
import com.aceinteract.android.sleak.util.LocalLibraryUtil




class MediaNotificationManager(private val service: MusicService) {

    private val localLibraryUtil = LocalLibraryUtil(service)

    private var playAction = NotificationCompat.Action(
        R.drawable.ic_round_play_arrow,
        service.getString(R.string.desc_np_play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY)
    )

    private var pauseAction = NotificationCompat.Action(
        R.drawable.ic_round_pause,
        service.getString(R.string.desc_np_pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PAUSE)
    )

    private var nextAction = NotificationCompat.Action(
        R.drawable.ic_round_skip_next,
        service.getString(R.string.desc_np_next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    )

    private var prevAction = NotificationCompat.Action(
        R.drawable.ic_round_skip_previous,
        service.getString(R.string.desc_np_previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )

    val notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        notificationManager.cancelAll()
    }

    fun getNotification(metadata: MediaMetadataCompat, state: PlaybackStateCompat, token: MediaSessionCompat.Token): Notification {

        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING

        val description = metadata.description

        val builder = buildNotification(token, isPlaying, description)

        return builder.build()

    }

    private fun buildNotification(token: MediaSessionCompat.Token, isPlaying: Boolean,
                                  description: MediaDescriptionCompat): NotificationCompat.Builder {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel()

        val albumArt = localLibraryUtil.getAlbumArtBitmap(description.mediaId!!.toLong())

        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
        builder.setStyle(
            MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2))
            .setColor(ContextCompat.getColor(service, R.color.color_accent))
            .setSmallIcon(R.drawable.ic_sleak_transparent)
            .setContentIntent(createContentIntent())
            .setContentTitle(description.title)
            .setContentText(description.subtitle)
            .setSubText(localLibraryUtil.getSong(description.mediaId!!.toLong())!!.album)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVibrate(LongArray(0))
            .setLargeIcon(albumArt)
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.addAction(prevAction)
        builder.addAction(if (isPlaying) pauseAction else playAction)
        builder.addAction(nextAction)

        return builder

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {

        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val name = "MediaSession"
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.enableLights(true)
            channel.enableVibration(false)
            channel.lightColor = service.getColor(R.color.color_accent)

            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(service, MainActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return PendingIntent.getActivity(service, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    companion object {

        const val NOTIFICATION_ID = 200

        private val TAG = MediaNotificationManager::class.simpleName

        private const val CHANNEL_ID = "com.aceinteract.android.sleak.channel.music"

        private const val REQUEST_CODE = 300

    }

}