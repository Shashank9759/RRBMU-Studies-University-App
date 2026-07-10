package com.studies.rrbmustudies

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.studies.rrbmustudies.di.initKoin
import com.studies.rrbmustudies.di.presentationModule
import com.studies.rrbmustudies.navigation.IncomingDeepLink
import com.studies.rrbmustudies.navigation.parseAppDeepLink
import com.studies.rrbmustudies.di.androidAdsModule
import com.studies.rrbmustudies.platform.bindActivity
import com.studies.rrbmustudies.platform.bindAndroidContext
import com.studies.rrbmustudies.platform.initializeFirebase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MainActivity : ComponentActivity() {

    private var incomingDeepLink by mutableStateOf<IncomingDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Block screenshots / screen recording for the entire app (QA H3).
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        bindAndroidContext(this)
        bindActivity(this)
        initializeFirebase(applicationContext)
        applyDeepLink(intent)
        initKoin(extraModules = listOf(presentationModule, androidAdsModule)) {
            androidLogger()
            androidContext(this@MainActivity)
        }
        enableEdgeToEdge()
        setContent {
            App(initialDeepLink = incomingDeepLink)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyDeepLink(intent)
    }

    private fun applyDeepLink(intent: Intent?) {
        if (intent == null) return

        // 1) Custom scheme share / VIEW links: rrbmustudies://...
        val fromUri = intent.data?.let(::parseUriDeepLink)
        if (fromUri != null) {
            incomingDeepLink = fromUri
            return
        }

        // 2) FCM tray PendingIntent extras
        val notificationId = intent.getStringExtra("deep_link_notification_id")
            ?: intent.getStringExtra("notificationId")
            ?: intent.extras?.getString("notificationId")
        if (!notificationId.isNullOrBlank()) {
            incomingDeepLink = IncomingDeepLink.Notification(
                notificationId = notificationId,
                openAttachment = intent.getBooleanExtra("deep_link_open_attachment", false) ||
                    !intent.getStringExtra("attachmentUrl").isNullOrBlank() ||
                    !intent.getStringExtra("linkUrl").isNullOrBlank(),
            )
        }
    }

    private fun parseUriDeepLink(uri: Uri): IncomingDeepLink? {
        val segments = uri.pathSegments.orEmpty()
        return parseAppDeepLink(
            scheme = uri.scheme,
            host = uri.host,
            pathSegments = segments,
        )
    }
}
