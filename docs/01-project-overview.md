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

# 01 — Project Overview

## What is this app?

**RRBMU Studies** is a Kotlin Multiplatform mobile app for students of
Raj Rishi Bhartrihari Matsya University (RRBMU). The app gives students a
single place to:

- Browse every course offered by the university (UG, PG, Diploma).
- See past-year question papers organised by course → system → part → subject.
- Download those papers as PDFs to read offline (a personal "vault").
- Read official notifications from the university with attachments.
- Search across all courses and papers.
- Send feedback to the app team.

It also has a hidden **admin mode** that university staff can use inside the same
app to upload courses, papers, home banners, and notifications — no separate
admin website is needed.

---

## Who uses it?

There are two kinds of users. The UI adapts based on which one you are.

| User type | How they log in | What they can do |
|-----------|-----------------|------------------|
| **Student** (default) | No login needed | Browse, search, download papers, read notifications, send feedback |
| **Admin** | Firebase email + password | Everything a student can, plus: create/edit courses, upload papers, publish notifications, manage home banners |

Whether someone is an admin is decided by whether their Firebase UID exists in
the `admins` collection in Firestore. Only two facts about an admin are
persisted app-side: their email and their display name.

---

## The big picture in one paragraph

The app opens on a **splash screen** for 800 ms, then goes to a
**Main scaffold** with four bottom tabs: **Home, Courses, Notifications, More**.
From those tabs the user drills down into a **Course** → picks a **System** (yearly,
semester or entrance) → opens a **Part** (e.g. Year 1) → sees a list of **Papers**
→ opens a **Paper Detail** page → can either preview the PDF online or download it
to a local **Vault**. Search, notifications, settings, feedback, about and the
admin panel are all reachable from either the top bar or the "More" tab.

---

## What makes it interesting technically

- **One codebase, two platforms.** Written in Kotlin Multiplatform with
  Compose Multiplatform for the UI. Same UI code runs on Android and iOS.
- **Clean architecture with three Gradle modules.** `domain` (pure Kotlin,
  no framework), `data` (Firebase, SQLDelight, Settings), and `composeApp`
  (the UI). A tiny `shared` module wires DI.
- **Firebase-only backend.** No custom REST server. Firestore holds all data,
  Firebase Storage holds the PDFs and images, Firebase Auth handles admin
  login, Firebase Analytics tracks events, and one Cloud Function turns new
  notifications into push messages.
- **Offline-first for PDFs.** SQLDelight remembers which papers have been
  downloaded; the file bytes live in the app's private folder.
- **Type-safe navigation.** Uses AndroidX Navigation Compose with
  `@Serializable` route classes — no string routes.
- **Deep links.** Notifications and papers can be opened from a share sheet
  via `rrbmustudies://` URIs.
- **AdMob monetisation.** Banner slots on many screens plus rewarded /
  interstitial ads on paper opens; wrapped in a `PaperAdGateway`
  interface so iOS can no-op.
- **Push notifications via FCM.** Users subscribe to the `all_users` topic
  and get a push whenever an admin publishes a notification.

---

## The user's typical journey

1. Install the app from the Play Store.
2. See the splash logo for a moment.
3. Land on the Home tab. See course tiles and recent papers.
4. Tap a course, e.g. B.A.
5. Tap a system, e.g. "Semester".
6. Tap a part, e.g. "Semester 3".
7. Tap a paper, e.g. "Sociology – 2022".
8. Read the metadata, tap "View PDF" — an ad may play first.
9. Optionally tap "Download offline" to save it in the personal vault.
10. Later, go to **Settings → Downloaded Papers** to open it without internet.

An admin's journey adds:

1. From the **More** tab or **Settings**, open **Admin login**.
2. Enter email + password. If your UID is in `admins`, you're now an admin.
3. See the admin FAB on course, part and dashboard screens.
4. Create a course, upload a paper PDF, or push a notification.

---

## Non-goals

- No user accounts for students (browsing is fully anonymous).
- No comments, chat, forum or social features.
- No offline mode for course lists or notifications — only PDFs are downloaded.
- No cross-device sync — every device has its own local vault.

The app is intentionally **read-only for students** and **content-management
only for admins**. Everything else is out of scope.
