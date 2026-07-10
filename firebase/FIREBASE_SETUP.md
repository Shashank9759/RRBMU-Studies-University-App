# RRBMU Studies — Firebase Setup (new project)

Use a **new Google account** and a **new Firebase project**. The old `rrbmu-studies-18e01` project is not required.

**What Firebase is for in this app:** papers (PDF list + files), optional home ads, notifications, feedback, and admin login.  
**What is NOT in Firebase:** all 31 courses, systems, and parts — those are bundled in the app (`CourseSeed.kt`).

---

## 1. Create a new Firebase project

1. Sign in to [Firebase Console](https://console.firebase.google.com/) with your **new Google account**
2. Click **Add project**
3. Project name: e.g. `RRBMU Studies` (any name you like)
4. **Project ID:** pick something unique, e.g. `rrbmu-studies-prod` — you will use this everywhere below
5. Disable Google Analytics if you do not need it (optional)
6. Wait for the project to finish creating

---

## 2. Enable products

In the new project:

| Product | Where | Settings |
|---------|--------|----------|
| **Authentication** | Build → Authentication → Get started | Enable **Email/Password** only |
| **Firestore** | Build → Firestore Database → Create database | **Production mode** (we deploy rules next) |
| **Storage** | Build → Storage → Get started | Default bucket is fine |

You do **not** need Realtime Database for this app.

---

## 3. Register the Android app

1. Project settings (gear) → **Your apps** → **Add app** → Android
2. Android package name: `com.studies.rrbmustudies` (must match the app)
3. App nickname: `RRBMU Studies` (optional)
4. Download **`google-services.json`**
5. Replace the file in the repo:

   ```
   composeApp/google-services.json
   ```

6. Rebuild the app: `./gradlew :composeApp:assembleDebug`

### SHA fingerprints (add these in Firebase)

Firebase Console → **Project settings** → your Android app → **Add fingerprint**

| When | Required? |
|------|-----------|
| **Email/Password admin login** (this app) | Not strictly required, but **add them anyway** — Firebase recommends it |
| **Google Sign-In / Phone Auth** | Required |
| **Play Store release** | Add **upload key** + **Play App Signing** SHA from Play Console |

**Debug build** (local testing on your machine):

```bash
./gradlew :composeApp:signingReport
```

Under `Variant: debug`, copy **SHA-1** and **SHA-256** into Firebase.

**Release build** (when you publish to Play Store):

1. Create a release keystore and configure `signingConfigs` in `composeApp/build.gradle.kts`
2. Run `./gradlew :composeApp:signingReport` again and add the **release** SHA-1 + SHA-256
3. In [Google Play Console](https://play.google.com/console) → your app → **Setup** → **App integrity** → copy **App signing key certificate** SHA-1 and SHA-256 and add those to Firebase too

Add **both** SHA-1 and SHA-256 for each keystore (debug + release + Play signing).

---

## 4. Deploy security rules and indexes

Install [Firebase CLI](https://firebase.google.com/docs/cli) if needed:

```bash
npm install -g firebase-tools
```

From the **repository root**:

```bash
firebase login
firebase use --add
# Select your NEW project ID and alias it "default"

firebase deploy --only firestore:rules,firestore:indexes,storage
```

Rules and indexes live in:

- `firebase/firestore.rules`
- `firebase/firestore.indexes.json`
- `firebase/storage.rules`

Copy `.firebaserc.example` → `.firebaserc` and set your project ID if you prefer editing the file instead of `firebase use --add`.

---

## 5. Create an admin user

### Step A — Auth user

Firebase Console → **Authentication** → **Users** → **Add user**

Example: `admin@yourdomain.com` / strong password

Copy the user's **UID**.

### Step B — Admin document (required)

Firestore → **Start collection** → collection ID: `admins` → Document ID = **that UID**:

```json
{
  "email": "admin@yourdomain.com",
  "displayName": "Admin",
  "role": "admin"
}
```

Without this document, login works in Auth but the app treats the user as non-admin and uploads will fail.

> Admin docs cannot be created from the app (rules block client writes). Always create them in the Firebase Console.

---

## 6. Firestore data structure

**Papers only** (uploaded via app or scripts):

```
courses/{courseId}/systems/{systemId}/parts/{partId}/papers/{paperId}
```

**Storage PDF path:**

```
papers/{courseId}/{systemId}/{partId}/{paperId}.pdf
```

**IDs** must match bundled data, e.g. `bsc` / `yearly_system` / `part_1` (see `CourseIds.kt`, `SystemIds.kt`, `CourseSeed.kt`).

**Optional collections:**

| Collection | Purpose |
|------------|---------|
| `home_ads` | Override home carousel (app has built-in fallback) |
| `notifications` | Alerts tab |
| `feedback` | User feedback from the app |

### Paper document (created on upload)

```json
{
  "title": "BSC-PART-1-CHEMISTRY-P1-INORGANIC-CHEMISTRY-706-A-2023",
  "subject": "Chemistry",
  "paperCode": "706-A",
  "year": 2023,
  "description": null,
  "pdfUrl": "https://firebasestorage.googleapis.com/...",
  "coverImageUrl": null,
  "downloadCount": 0,
  "isPublished": true,
  "createdAt": 1710000000000,
  "updatedAt": 1710000000000,
  "createdBy": "admin-uid"
}
```

---

## 7. Optional: seed course skeleton in Firestore

The app **does not need** course documents in Firestore for Home/Courses to work.

If you want the same tree in Firestore (for admin tools or consistency):

1. Project settings → **Service accounts** → **Generate new private key** → save JSON
2. Run:

```bash
cd scripts
npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccount.json
export FIREBASE_PROJECT_ID=your-new-project-id
npm run seed
```

---

## 8. Upload a paper from the app

1. Install the app with the **new** `google-services.json`
2. **More** → **Settings** → **Admin Login** → sign in
3. **Courses** → course → system → part → orange **+** FAB
4. Fill title, subject, paper code, year → select PDF → **Save & Publish**

Upload flow: Storage first, then Firestore paper doc with download URL.

---

## 9. Verify

| Check | Location |
|-------|----------|
| PDF uploaded | Storage → `papers/bsc/yearly_system/part_1/...` |
| Paper metadata | Firestore → `courses/bsc/systems/yearly_system/parts/part_1/papers/` |
| Admin user | Authentication → Users |
| Admin role | Firestore → `admins/{uid}` |
| Indexes | Firestore → Indexes (should show deployed composites) |

---

## 10. Troubleshooting

| Problem | Fix |
|---------|-----|
| App still hits old project | Replace `composeApp/google-services.json`, clean rebuild |
| Permission denied on read | `firebase deploy --only firestore:rules,storage` |
| Permission denied on upload | User must have `admins/{uid}` doc; deploy storage rules |
| Empty course list | Courses are bundled — not a Firebase issue; check app build |
| Search / recent papers empty | Deploy indexes; ensure `isPublished: true` on papers |
| Index required error in logcat | Run `firebase deploy --only firestore:indexes` or use Console link |

---

## 11. iOS (when enabled)

1. Add iOS app in Firebase Console (same project)
2. Download `GoogleService-Info.plist` → `composeApp/iosApp/`
3. Same Auth, Firestore, Storage, rules, and admin setup

---

## Files to update when switching projects

| File | Action |
|------|--------|
| `composeApp/google-services.json` | Replace with download from new project |
| `.firebaserc` | Set `default` to new project ID |
| `scripts/seed-courses.js` | Uses `FIREBASE_PROJECT_ID` env var |

No Kotlin source changes are needed — the app reads project config from `google-services.json` only.
