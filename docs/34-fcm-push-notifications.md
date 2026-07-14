<style>
/* Ensure the markdown preview is scrollable both directions */
html, body { overflow: auto !important; height: auto; max-height: none; }
pre {
  overflow-x: auto !important;
  overflow-y: auto !important;
  max-width: 100%;
  white-space: pre;
  word-wrap: normal;
}
code { white-space: pre; }
table {
  display: block;
  overflow-x: auto;
  max-width: 100%;
  white-space: nowrap;
}
img { max-width: 100%; height: auto; }
</style>

# 34 — FCM Push Notifications

Push notifications go from an admin's tap → Firestore → Cloud Function →
FCM `all_users` topic → every installed device.

---

## 1. Who publishes a push

Admins do it via `ManageNotificationsAdminScreen`:

1. Fill in title + body + optional attachment + category.
2. Flip `isActive` to `true`.
3. Save.

That writes a doc to `notifications/{id}` with `isActive = true`.

---

## 2. The Cloud Function — `functions/index.js`

Triggered by any write to `notifications/{id}` in region `asia-south1`.

Logic (already covered in 08-firebase-backend.md, quick recap):

- If `after` is null → doc deleted → skip.
- If `after.isActive !== true` → skip.
- If it was already active and payload is unchanged → skip.
- Otherwise `admin.messaging().send(message)` with:
  - `topic = "all_users"`
  - `notification { title, body }`
  - `data { type, notificationId, title, body, attachmentUrl, linkUrl,
    pageUrl, category }`
  - `android { priority "high", notification { channelId "notifications",
    clickAction "OPEN_NOTIFICATION" } }`

Sending fails only in unusual cases (quota, invalid credentials).

---

## 3. Subscription on device

`RrbmuMessagingService` (Android) subscribes automatically:

```kotlin
FirebaseMessaging.getInstance().subscribeToTopic("all_users")
```

iOS uses `FIRMessaging.messaging().subscribeToTopic("all_users")` from the
iOS wrapper (typically inside the App delegate or when Firebase is
initialised).

---

## 4. Android — `RrbmuMessagingService`

`composeApp/androidMain/kotlin/.../messaging/RrbmuMessagingService.kt`.

Registered in `AndroidManifest.xml`:

```xml
<service
    android:name=".messaging.RrbmuMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### `onMessageReceived(message)`

1. Read `notificationId`, `title`, `body`, `attachmentUrl`, `type` from
   `message.data` (with `message.notification` as fallback for title/body).
2. If `type` is present and not `"notification"`, skip (reserved for
   future non-notification pushes).
3. `postNotification(...)`:
   - Ensures the `notifications` channel exists (`NotificationChannel`
     with importance HIGH on API 26+).
   - Builds a `NotificationCompat` with:
     - Small icon: `R.drawable.ic_stat_rrbmu` (monochrome).
     - Large icon: full-colour `R.drawable.univ_logo`.
     - Colour tint: `#1A237E` (brand navy).
     - Title + body (BigText style so long bodies are readable).
     - Auto-cancel on tap.
   - Content intent: `Intent(MainActivity)` with action
     `"OPEN_NOTIFICATION"` and extras `deep_link_notification_id`,
     `deep_link_open_attachment`.
   - Posts via `NotificationManager.notify(notificationId.hashCode(), n)`.

### `onNewToken(token)`

Currently a no-op (`super.onNewToken(token)`). Devices are addressed via
the `all_users` topic subscription, so we don't need to send the token
anywhere.

---

## 5. From tap → in-app screen

The pending intent launched by `RrbmuMessagingService` reopens
`MainActivity`. `MainActivity.onCreate` and `onNewIntent` both call
`buildDeepLinkFromIntent(intent)`:

```kotlin
private fun buildDeepLinkFromIntent(intent: Intent?): IncomingDeepLink? {
    val id = intent?.getStringExtra("deep_link_notification_id") ?: return null
    val openAttachment = intent.getBooleanExtra("deep_link_open_attachment", false)
    return IncomingDeepLink.Notification(id, openAttachment)
}
```

That gets passed into `App(initialDeepLink = …)` → `AppNavHost`, which
uses the deep-link handling described in **11-navigation.md** to navigate
to `NotificationDetailRoute`.

---

## 6. iOS side

- The iOS wrapper registers for remote notifications and subscribes to the
  `all_users` topic on launch.
- APNs delivers the push; the OS displays the banner using the FCM
  notification payload.
- Tapping the notification opens the app; the Swift AppDelegate parses the
  data payload and calls the shared Kotlin API to build an
  `IncomingDeepLink.Notification` — same downstream path as Android.

---

## 7. Setup checklist for a new Firebase project

Documented in `firebase/FCM_SETUP.md`, summarised here:

1. In Firebase Console: enable Cloud Messaging + Cloud Functions.
2. Deploy the Cloud Function:
   ```
   cd functions && npm install
   firebase deploy --only functions
   ```
3. Deploy Firestore + Storage rules and indexes:
   ```
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```
4. Add `google-services.json` under `composeApp/`.
5. Add `GoogleService-Info.plist` to the iOS Xcode project.
6. Enable APNs certificate/key in the Firebase Cloud Messaging settings.

Once configured, publishing a notification through the app immediately
pings every installed device.
