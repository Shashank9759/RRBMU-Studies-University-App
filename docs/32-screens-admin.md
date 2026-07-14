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

# 32 — Admin Screens

Everything an admin can do. There are seven admin-only screens plus one
bottom sheet, all under `presentation/admin/`.

- **Admin Login (bottom sheet + standalone screen)**
- **Admin Dashboard**
- **Manage Courses**
- **Manage Home Ads**
- **Manage Notifications**
- **Upload Paper**

The admin status is decided by `AuthRepository.isAdmin` (see
06-domain-layer.md and 08-firebase-backend.md).

---

## 1. Admin Login

### 1.1 Standalone screen — `AdminLoginScreen`
**Route:** `AdminLoginRoute`.
**File:** `presentation/admin/AdminScreens.kt`.

- Top bar with back button.
- Email + password fields (password with show/hide eye).
- Error text.
- **Sign In** button (disabled if any field is empty or `isLoading`).
- Instructions row: "Sign in with your administrator account. Students do not need to sign in."

### 1.2 Bottom sheet — `AdminLoginBottomSheet`
**File:** `ui/components/AdminLoginBottomSheet.kt`.

Same content, hosted inside a `ModalBottomSheet`. Used from `SettingsScreen`
so the admin doesn't leave the current screen after signing in.

### 1.3 `AdminLoginViewModel`
```kotlin
data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)
```

Injected: `SignInAdminUseCase`.

Actions:
- `onEmailChange`, `onPasswordChange`, `togglePasswordVisibility`, `reset()`.
- `signIn()`:
  1. Set `isLoading = true`, clear error.
  2. Call `SignInAdminUseCase(email, password)` → Result.
  3. On success set `isSuccess = true`. On failure set `error = msg`.

`AdminLoginScreen` collects `isSuccess`; when it becomes `true`, `onLoginSuccess()`
is called and the login is popped off the back stack:

```kotlin
navController.navigate(AdminDashboardRoute) {
    popUpTo(AdminLoginRoute) { inclusive = true }
}
```

### 1.4 Under the hood

`SignInAdminUseCase` → `AuthRepositoryImpl.signIn(email, password)` →
`FirestoreAuthDataSource.signIn`:

1. `Firebase.auth.signInWithEmailAndPassword(email, password)`.
2. `firestore.document("admins/${uid}").get()`.
3. If that doc **does not exist**, auto-signs-out and returns `Result.failure`
   ("This account is not an admin.").
4. Otherwise returns `Result.success(AdminUser(uid, email, displayName))`.

`isAdmin` and `currentUser` reactive flows update immediately.

---

## 2. Admin Dashboard

**Route:** `AdminDashboardRoute`.
**File:** `presentation/admin/AdminScreens.kt` — `AdminDashboardScreen`.

Displays:
- Top bar with back button and the admin's email as subtitle.
- List of tiles:
  - Manage Courses → `ManageCoursesAdminRoute`
  - Manage Home Ads → `ManageHomeAdsRoute`
  - Manage Notifications → `ManageNotificationsAdminRoute`
  - Upload Paper → `UploadPaperRoute()` (empty defaults — admin picks the
    course/system/part inside)
  - Sign Out — calls `adminViewModel.signOut()`, then `popBackStack()`.

No ViewModel of its own. Uses `AdminViewModel` for signOut.

---

## 3. Manage Courses

**Route:** `ManageCoursesAdminRoute`.
**Files:** `ManageCoursesScreen.kt` + `ManageCoursesViewModel.kt`.

### What it displays
- Grouped course list identical in shape to the student Courses tab
  (`LevelSectionHeader` + rows).
- Each row has an edit icon → opens the **Edit Course** bottom sheet.
- **FAB (+)** → opens the **Create Course** bottom sheet.

### Edit / Create form
- Fields: `id` (create only), `name`, `shortName`, `level` (chips),
  `durationYears` (int selector), `order`, `isActive` (switch).
- **Image picker** for `backgroundImageUrl`. Uses
  `ImagePicker` — a platform-specific `expect fun rememberImagePicker(...)`.
- **Save** button.

### `ManageCoursesViewModel`

Injected: `GetAllCoursesUseCase`, `CreateCourseUseCase`, `UpdateCourseUseCase`,
`UploadCourseBackgroundUseCase`.

Exposes `state: StateFlow<UiState<List<Course>>>` and helpers:
- `uploadBackground(bytes, fileName, courseId)` → returns URL.
- `saveCourse(course, isNew)` → `create` or `update`.
- `refresh()`, `clearUploadedBackground()`.

### Under the hood
- Creating a course also triggers **`seedCourseSkeleton(courseId)`** in the
  data layer, which auto-generates the yearly/semester/entrance systems and
  the corresponding parts.
- Background image is uploaded to `course_backgrounds/{courseId}.{ext}` and
  the returned URL is stored in `Course.backgroundImageUrl`.

---

## 4. Manage Home Ads

**Route:** `ManageHomeAdsRoute`.
**File:** `AdminScreens.kt` → `ManageHomeAdsScreen`.
**ViewModel:** `ManageHomeAdsViewModel`.

### What it displays
- Top bar with title "Home Banners" and an add button.
- List of ad cards (image + title + description + status badge).
- Reorder up/down/top buttons on each card.
- Tap card → **Edit Ad** dialog.

### Edit Ad dialog
- Image picker.
- `title` (required).
- `description`.
- `linkUrl` (must be http/https or empty).
- `isActive` switch.
- Save / Delete buttons.

### Carousel settings section
- Number picker (2..60 s) for `slideIntervalSeconds`.

### `ManageHomeAdsViewModel`

Injected: `GetAllHomeAdsUseCase`, `CreateHomeAdUseCase`, `UpdateHomeAdUseCase`,
`DeleteHomeAdUseCase`, `UploadHomeAdImageUseCase`, `ReorderHomeAdsUseCase`,
`ObserveHomeCarouselSettingsUseCase`, `SaveCarouselSettingsUseCase`.

State:
- `state: StateFlow<UiState<List<HomeAd>>>`
- `actionMessage: StateFlow<String?>` (snackbar)
- `isUploadingImage: StateFlow<Boolean>`
- `uploadedImageUrl: StateFlow<String?>`
- `carouselSettings: StateFlow<HomeCarouselSettings>`

Actions:
- `uploadImage(bytes, fileName)` → temp `uploadedImageUrl`.
- `saveAd(ad)` — validates (image required, valid URL, non-blank title),
  then create/update.
- `deleteAd(id)`.
- `moveAdUp(id)`, `moveAdDown(id)`, `moveAdToTop(id)` — batch-write reorder.
- `saveCarouselInterval(seconds)`.

### Under the hood
- Image → `ads/{generatedId}.{ext}`.
- Reordering writes a Firestore batch of `{ order: N }` updates.
- The Home carousel picks up changes live (it observes `getActiveAds()` +
  `observeCarouselSettings()`).

---

## 5. Manage Notifications

**Route:** `ManageNotificationsAdminRoute`.
**File:** `AdminScreens.kt` → `ManageNotificationsAdminScreen`.
**ViewModel:** `ManageNotificationsViewModel`.

### What it displays
- Top bar with title and add button.
- List of notification cards (title + preview + status badge + timestamp).
- Tap card → **Edit Notification** dialog.

### Edit dialog
- `title` (required), `body`.
- Attachment picker — `NotificationAttachmentPicker` (`expect` for image or PDF).
- `category` chips.
- `isActive` switch — flipping this to `true` triggers the Cloud Function
  to send an FCM push.
- Save / Delete buttons.

### `ManageNotificationsViewModel`

Injected: `GetAllNotificationsUseCase`, `CreateNotificationUseCase`,
`UpdateNotificationUseCase`, `DeleteNotificationUseCase`,
`UploadNotificationAttachmentUseCase`.

Same structure as `ManageHomeAdsViewModel` — `state`, `actionMessage`,
`isUploadingAttachment`, `uploadedAttachmentUrl`, plus the CRUD methods.

### Under the hood
- Attachment → `notification_attachments/{generatedId}.{ext}` (pdf / jpg /
  png / webp / gif).
- On write, Cloud Function `onNotificationWrite` in `functions/index.js`
  fires and pushes an FCM to `all_users` topic (see 34-fcm-push-notifications.md).

---

## 6. Upload Paper

**Route:** `UploadPaperRoute(courseId, systemId, partId)` — args are default
empty; the form asks the admin to pick them when empty.
**File:** `AdminScreens.kt` → `UploadPaperScreen`.
**ViewModel:** `UploadPaperViewModel`.

### What it displays
- Top bar with title.
- Breadcrumb showing the selected course / system / part.
- Cascading dropdowns for course → system → part (if not pre-filled from the
  route args).
- Fields: `title`, `subject`, `paperCode`, `year`, optional `description`.
- **PDF picker** — `PdfPicker` (`expect fun rememberPdfPicker(...)`).
  Shows the selected file name and size.
- Bottom bar with **Save Draft** and **Save & Publish** buttons.
- Upload progress indicator.
- Snackbar for errors / success.

### `UploadPaperViewModel`

Constructor:
```kotlin
class UploadPaperViewModel(
    private val getAllCourses: GetAllCoursesUseCase,
    private val getSystems: GetCourseSystemsUseCase,
    private val getParts: GetPartsUseCase,
    private val uploadPaper: UploadPaperUseCase,
    private val updatePaper: UpdatePaperUseCase,
    private val courseId: String,
    private val systemId: String,
    private val partId: String,
) : ViewModel()
```

State: form fields + `isSubmitting: Boolean` + `error: String?`
+ `isDone: Boolean`.

Actions:
- Setters for each form field.
- `selectCourse(id)`, `selectSystem(id)`, `selectPart(id)` — cascade-load
  child data.
- `submit(isPublished: Boolean, pdfBytes: ByteArray?, pdfFileName: String?)`
  — validates required fields + PDF, then `UploadPaperUseCase(paper, bytes, name)`.

### Under the hood
- PDF is uploaded to Firebase Storage at
  `papers/{courseId}/{systemId}/{partId}/{paperId}.pdf` with content type
  `application/pdf`.
- Paper doc is written to Firestore with the resulting `pdfUrl`.
- `Part.paperCount` is *not* incremented automatically — admins are on the
  hook for keeping that in sync, or a Cloud Function could be added later.

---

## 7. Pickers (`expect` composables)

Three platform-specific pickers are used across admin screens:

- **`rememberImagePicker(onPicked: (bytes, fileName) -> Unit): () -> Unit`**
- **`rememberPdfPicker(onPicked: (bytes, fileName) -> Unit): () -> Unit`**
- **`rememberNotificationAttachmentPicker(onPicked: (bytes, fileName) -> Unit): () -> Unit`**

### Android impls
Under `androidMain/presentation/admin/`. Use
`ActivityResultContracts.OpenDocument` with the appropriate MIME filter
(`image/*`, `application/pdf`, or `image/*|application/pdf`). The result is
read as bytes via `ContentResolver.openInputStream`.

### iOS impls
Under `iosMain/presentation/admin/`. Use `UIDocumentPickerViewController`
(for PDFs) and `PHPickerViewController` (for images). Returns bytes via
`NSData.bytes` copy.

That is every screen and every admin flow. The remaining docs cover
cross-cutting concerns.
