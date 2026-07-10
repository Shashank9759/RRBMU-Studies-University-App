package com.studies.rrbmustudies.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

private const val FCM_TOPIC = "all_users"
private const val NOTIFICATION_CHANNEL_ID = "notifications"

fun initializeFirebase(context: Context) {
    Firebase.initialize(context)
    ensureNotificationChannel(context)

    // Subscribe every device (guest + admin) to a shared topic.
    // The Cloud Function publishes to this topic whenever an Active alert is saved.
    try {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(FCM_TOPIC)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.w("RRBMU", "FCM topic subscribe failed", task.exception)
                }
            }
    } catch (t: Throwable) {
        android.util.Log.w("RRBMU", "FCM topic subscribe error", t)
    }
}

/**
 * Channel must exist before a background FCM "notification" payload arrives —
 * otherwise Android 8+ can drop the system tray alert silently.
 */
private fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) return

    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "University Alerts",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Exam notices, timetable and university announcements"
        enableVibration(true)
    }
    manager.createNotificationChannel(channel)
}
