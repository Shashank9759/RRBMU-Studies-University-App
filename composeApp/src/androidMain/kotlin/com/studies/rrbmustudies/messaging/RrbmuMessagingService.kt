package com.studies.rrbmustudies.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.studies.rrbmustudies.MainActivity
import com.studies.rrbmustudies.R

private const val CHANNEL_ID = "notifications"
private const val EXTRA_DEEP_LINK_NOTIFICATION_ID = "deep_link_notification_id"

class RrbmuMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val notification = message.notification

        val notificationId = data["notificationId"]
            ?.takeIf { it.isNotBlank() }
            ?: data["id"]
            ?: return

        val title = data["title"]
            ?.takeIf { it.isNotBlank() }
            ?: notification?.title
            ?: "RRBMU Studies"
        val body = data["body"]
            ?.takeIf { it.isNotBlank() }
            ?: notification?.body
            ?: ""

        val attachmentUrl = sequenceOf(
            data["attachmentUrl"],
            data["linkUrl"],
            data["pageUrl"],
        ).firstOrNull { !it.isNullOrBlank() }.orEmpty()

        // Always show a tray notification when we receive an FCM payload.
        // (Previously we required type=notification and skipped other messages.)
        val type = data["type"].orEmpty()
        if (type.isNotBlank() && type != "notification") return

        postNotification(
            context = this,
            notificationId = notificationId,
            title = title,
            body = body,
            openAttachment = attachmentUrl.isNotBlank(),
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun postNotification(
        context: Context,
        notificationId: String,
        title: String,
        body: String,
        openAttachment: Boolean,
    ) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "OPEN_NOTIFICATION"
            putExtra(EXTRA_DEEP_LINK_NOTIFICATION_ID, notificationId)
            putExtra("deep_link_open_attachment", openAttachment)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Full-colour university seal shown as the large icon on the right of the notification.
        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.univ_logo)
        }.getOrNull()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_rrbmu)
            .setColor(Color.parseColor("#1A237E"))
            .apply { largeIcon?.let { setLargeIcon(it) } }
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "University Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Exam notices, timetable and university announcements"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }
}
