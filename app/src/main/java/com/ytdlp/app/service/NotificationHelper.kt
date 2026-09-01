package com.ytdlp.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ytdlp.app.R
import com.ytdlp.app.ui.MainActivity

object NotificationHelper {
    const val CHANNEL_DOWNLOADS = "ytdlp_downloads_channel"
    const val CHANNEL_COMPLETED = "ytdlp_completed_channel"
    const val NOTIFICATION_ID_FOREGROUND = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOADS,
                "Active Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live download progress and speed"
                enableVibration(false)
                setSound(null, null)
            }

            val completedChannel = NotificationChannel(
                CHANNEL_COMPLETED,
                "Download Completed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when downloads finish"
            }

            notificationManager.createNotificationChannel(downloadChannel)
            notificationManager.createNotificationChannel(completedChannel)
        }
    }

    fun buildForegroundNotification(
        context: Context,
        title: String,
        progress: Int,
        speed: String,
        eta: String
    ): NotificationCompat.Builder {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subText = buildString {
            if (speed.isNotBlank()) append(speed)
            if (speed.isNotBlank() && eta.isNotBlank()) append(" • ")
            if (eta.isNotBlank()) append("ETA: $eta")
        }

        return NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setContentTitle(title)
            .setContentText(if (subText.isNotBlank()) subText else "Downloading...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
    }

    fun showCompletedNotification(context: Context, notificationId: Int, title: String, filePath: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETED)
            .setContentTitle("Download Complete")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
