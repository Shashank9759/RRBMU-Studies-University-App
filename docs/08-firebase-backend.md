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

# 08 — Firebase Backend

The entire backend for RRBMU Studies is Firebase. There is no custom server.
This document covers:

1. Firestore collections and their document shapes.
2. Firebase Storage paths.
3. Firestore security rules.
4. Storage security rules.
5. Firestore composite indexes.
6. The one Cloud Function.
7. The Node seed script.

---

## 1. Firestore collections

The data lives under one Firestore database in region **asia-south1**. Full
tree:

```
firestore/
├── courses/{courseId}
│   ├── (course fields)
│   └── systems/{systemId}
│       ├── (system fields)
│       └── parts/{partId}
│           ├── (part fields)
│           └── papers/{paperId}
│               └── (paper fields)
│
├── home_ads/{adId}
│   └── (home ad fields)
│
├── app_settings/
│   └── home_carousel (singleton doc)
│
├── notifications/{notificationId}
│   └── (notification fields)
│
├── feedback/{feedbackId}
│   └── (feedback fields)
│
└── admins/{uid}
    └── (admin profile)
```

### 1.1 `courses/{courseId}`
Written & read by `FirestoreCourseDataSource`.

```
name              (string)
shortName         (string)
iconUrl           (string, optional)
backgroundImageUrl(string, optional)
order             (int)
isActive          (bool)
level             ("UG" | "PG" | "DIPLOMA")
durationYears     (int, default 3)
```

### 1.2 `courses/{courseId}/systems/{systemId}`
```
name  (string, e.g. "Yearly", "Semester", "Entrance")
type  ("YEARLY" | "SEMESTER" | "ENTRANCE")
```

There are usually 3 systems per course: `yearly`, `semester`, `entrance`.

### 1.3 `courses/{courseId}/systems/{systemId}/parts/{partId}`
```
name       (string, e.g. "Year 1", "Semester 3", "Entrance")
order      (int)
paperCount (int, denormalised)
description(string, optional)
```

### 1.4 `courses/.../parts/{partId}/papers/{paperId}`
```
title         (string)
subject       (string)
paperCode     (string)
year          (int)
description   (string, optional)
pdfUrl        (string, Firebase Storage download URL)
coverImageUrl (string, optional)
downloadCount (int)
isPublished   (bool)
createdAt     (int, epoch ms)
updatedAt     (int, epoch ms)
createdBy     (string, admin UID)
```

### 1.5 `home_ads/{adId}`
```
imageUrl    (string, https)
title       (string)
description (string, optional)
linkUrl     (string, https or empty)
order       (int)
isActive    (bool)
createdAt   (int, epoch ms)
```

### 1.6 `app_settings/home_carousel`
```
slideIntervalSeconds  (int, 2..60)
```

### 1.7 `notifications/{notificationId}`
```
title         (string)
body          (string)
imageUrl      (string, optional)
linkUrl       (string, optional)
pageUrl       (string, optional)
attachmentUrl (string, optional — PDF/image URL)
category      ("ANNOUNCEMENT" | "TIME_TABLE" | "IMPORTANT")
isActive      (bool)
createdAt     (int, epoch ms)
```

### 1.8 `feedback/{feedbackId}`
```
rating     (int, 1..5)
comment    (string)
tags       (array<string>)
deviceInfo (string, optional)
createdAt  (int, epoch ms)
```

### 1.9 `admins/{uid}`
```
email       (string)
displayName (string, optional)
role        (string, optional — currently unused by the app)
```

---

## 2. Firebase Storage paths

Bucket paths and what lives at each one:

| Path | Contents |
|------|----------|
| `papers/{courseId}/{systemId}/{partId}/{paperId}.pdf` | The paper PDF |
| `covers/…` | Reserved for paper cover images (rules allow, code path unused today) |
| `ads/{generatedId}.{ext}` | Home ad banner (jpg/png/webp/gif) |
| `course_backgrounds/{courseId}.{ext}` | Course hero background image |
| `notification_attachments/{generatedId}.{ext}` | PDF, image or file attached to a notification |

Content types are always set explicitly on upload so PDFs open in-browser
correctly.

---

## 3. Firestore security rules — `firebase/firestore.rules`

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isAdmin() {
      return request.auth != null &&
        exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }

    match /courses/{document=**} {
      allow read:  if true;
      allow write: if isAdmin();
    }

    // Collection-group query for /**/papers/* — needed for recent papers & search.
    match /{path=**}/papers/{paperId} {
      allow read: if true;
    }

    match /home_ads/{id} {
      allow read:  if resource.data.isActive == true || isAdmin();
      allow write: if isAdmin();
    }

    match /app_settings/{id} {
      allow read:  if true;
      allow write: if isAdmin();
    }

    match /notifications/{id} {
      allow read:  if true;
      allow write: if isAdmin();
    }

    match /feedback/{id} {
      allow create: if true;                              // students can submit
      allow read, update, delete: if isAdmin();           // only admins can see feedback
    }

    match /admins/{uid} {
      allow read:  if request.auth.uid == uid;            // admin can read only their own doc
      allow write: if false;                              // never writable from the app
    }
  }
}
```

### Key points

- **`isAdmin()`** uses `exists()` on `admins/{uid}`. That is the single source
  of truth for admin rights. Adding a Firebase user does *not* make them an
  admin — you must also add an `admins/{uid}` doc from the Firebase console.
- The extra `/{path=**}/papers/{paperId}` rule is **required** — without it,
  the recent-papers query and search would work locally but fail on the
  server with `PERMISSION_DENIED`.
- **Home ads** are visible to everyone only if `isActive == true` (draft ads
  stay admin-only).
- **Feedback** is write-only for students; only admins can read it.

---

## 4. Storage security rules — `firebase/storage.rules`

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    function isAdmin() {
      return request.auth != null &&
        firestore.exists(/databases/(default)/documents/admins/$(request.auth.uid));
    }

    match /papers/{allPaths=**}                 { allow read: if true; allow write: if isAdmin(); }
    match /covers/{allPaths=**}                 { allow read: if true; allow write: if isAdmin(); }
    match /ads/{allPaths=**}                    { allow read: if true; allow write: if isAdmin(); }
    match /course_backgrounds/{allPaths=**}     { allow read: if true; allow write: if isAdmin(); }
    match /notification_attachments/{allPaths=**} { allow read: if true; allow write: if isAdmin(); }
  }
}
```

Uses the same admin check but points to Firestore's `admins/{uid}`.

---

## 5. Firestore composite indexes — `firebase/firestore.indexes.json`

Required for the queries the app runs:

| Collection group | Fields | Used by |
|------------------|--------|---------|
| `papers` (collectionGroup) | `isPublished ASC, createdAt DESC` | Recent papers on Home |
| `papers` (collectionGroup) | `isPublished ASC, title ASC` | Alphabetical search |
| `home_ads` (collection) | `isActive ASC, order ASC` | Home ad carousel |
| `notifications` (collection) | `isActive ASC, createdAt DESC` | Notifications tab |

Deploy with `firebase deploy --only firestore:indexes`.

---

## 6. Cloud Function — `functions/index.js`

There is only one function. It fires on write to any `notifications/{id}` doc
and sends an FCM push to the topic `all_users`. Region `asia-south1`.

**Trigger:** `onDocumentWritten("notifications/{id}")`.

**Logic:**
1. If the doc has been deleted, exit.
2. If `after.isActive !== true`, exit (nothing to push about a draft).
3. If the doc was already active before *and* the payload (title, body,
   attachment) is unchanged, exit — this prevents duplicate pushes when an
   admin only tweaks a minor field.
4. Otherwise build an FCM `Message` with:
   - `topic: "all_users"`.
   - `notification { title, body }`.
   - `data { type, notificationId, title, body, attachmentUrl, linkUrl, pageUrl, category }`.
   - `android { priority: "high", notification { channelId: "notifications", clickAction: "OPEN_NOTIFICATION" } }`.
5. Send via `admin.messaging().send(message)`.

The `data` payload is what the client parses to deep-link into the
notification detail screen (`rrbmustudies://notification/{id}`).

---

## 7. Seed script — `scripts/seed-courses.js`

Bootstraps the 31 built-in courses in `courses/{id}` and creates the
`systems/{yearly, semester, entrance}` skeleton for each one. Papers are
**not** seeded — admins upload those in-app.

Run manually with:

```
cd scripts
npm install
node seed-courses.js
```

You need a Firebase service account key on `GOOGLE_APPLICATION_CREDENTIALS`.

---

## 8. Firebase setup docs

There are two Markdown files in `firebase/` that walk you through setting up
a new Firebase project from zero:

- `firebase/FIREBASE_SETUP.md`
- `firebase/FCM_SETUP.md`

Read those if you are configuring a fresh Firebase project.

That is the entire backend.
