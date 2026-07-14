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

# 28 — Screen: Feedback

**Files:**
- `presentation/feedback/FeedbackScreen.kt`
- `presentation/feedback/FeedbackViewModel.kt`

A simple form for students to send feedback. Writes to the Firestore
`feedback` collection, which only admins can read.

---

## 1. Route

```kotlin
@Serializable
data object FeedbackRoute
```

Reached from the More tab and from the About screen.

---

## 2. What it displays

1. **`FeedbackHeroIllustration`** at the top.
2. Headline: "We value your feedback".
3. **`EmojiRatingBar`** — 5 emoji faces representing 1..5 stars.
4. **`FeedbackTagChipRow`** — multi-select chips like "Design", "Speed",
   "Content", "Ads too much", etc.
5. **`OutlinedTextField`** for a free-form comment (multi-line, max ~500
   chars).
6. **Submit button** — disabled unless `rating > 0`.
7. **Success state** — after submit, the whole form is replaced with a
   thank-you card and a "Back to app" button.

---

## 3. State — `FeedbackViewModel`

```kotlin
data class FeedbackUiState(
    val rating: Int = 0,
    val comment: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
)
```

Injected: `SubmitFeedbackUseCase`.

### Actions

- `onRatingChange(rating: Int)`
- `onCommentChange(comment: String)`
- `onTagToggle(tag: String)`
- `submit()` — validates `rating > 0`, then calls
  `SubmitFeedbackUseCase(Feedback(rating, comment, tags.toList()))`.
  Sets `isSubmitting = true` while pending; sets `isSubmitted = true` on
  success or `error = msg` on failure.

---

## 4. Callback

Only one:
- `onBack()` — pop.

After a successful submit, the "Back to app" button also calls `onBack()`.

---

## 5. Storage rules recap

From `firestore.rules`:

```
match /feedback/{id} {
    allow create: if true;
    allow read, update, delete: if isAdmin();
}
```

Anyone can `create` a feedback doc, so a student never needs to log in.
Only admins can view / edit / delete them.

---

## 6. Data written to Firestore

```
feedback/{autoId}
  rating     : 1..5
  comment    : string
  tags       : ["design", "content", …]
  deviceInfo : string?    (currently null — could be set later)
  createdAt  : epoch ms
```

---

## 7. Non-obvious details

- **No user identifier is captured.** By design — this is anonymous.
- **`deviceInfo`** is nullable and unused today. A follow-up could populate
  it with `Build.MODEL` on Android for triage.
- **Submitting the same feedback twice** creates two docs. There is no
  deduplication or rate limit at the client — the security rules could be
  tightened to enforce a rate limit later.
