# FCM Push for Alerts (notifications collection)

This app already writes alerts into Firestore at:

`notifications/{id}`

To also show real system tray notifications for every user, deploy the Cloud Function in `functions/`.

## 1) Enable Firebase Cloud Functions
In Firebase Console → your project → **Build → Functions** → enable.

## 2) Install and deploy

```bash
cd functions
npm install
cd ..

firebase login
firebase use <your-project-id>

# Deploy function
npx firebase-tools deploy --only functions --project <your-project-id>
```

## 3) Required client behavior
The Android app subscribes to FCM topic `all_users` automatically (code change in this repo).

## 4) Notification click behavior
When the user taps the tray notification, the client opens:
`Notifications → Notification Details` for the matching `notificationId`.

