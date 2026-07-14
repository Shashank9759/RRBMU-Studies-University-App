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

# 30 — Screen: Legal Document

**File:** `presentation/more/LegalDocumentScreen.kt`

Static viewer for privacy policy / terms of service / disclaimer text.

---

## 1. Route

```kotlin
@Serializable
data class LegalDocumentRoute(val documentId: String)
```

`documentId` is one of `"privacy"`, `"terms"`, `"disclaimer"` — keys into
`AboutDefaults.legalDocuments: Map<String, LegalDocument>`.

---

## 2. What it displays

1. **Top bar** — back button + document title (e.g. "Privacy Policy").
2. **Scrollable body** — the Markdown-lite text of the document rendered as
   a `Column` of `Text` blocks. No HTML rendering — plain string with
   pre-baked paragraphs.

---

## 3. State

No ViewModel. Just:

```kotlin
@Composable
fun LegalDocumentScreen(documentId: String, onBack: () -> Unit) {
    val doc = AboutDefaults.legalDocuments[documentId] ?: AboutDefaults.legalDocuments["disclaimer"]!!
    Scaffold(topBar = { RrbmuTopBar(title = doc.title, onBack = onBack) }) {
        Column(...) {
            doc.paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
```

Where `LegalDocument(title: String, paragraphs: List<String>)`.

---

## 4. Where the content lives

Everything is hardcoded in `AboutDefaults.kt`. If the university needs a new
version, update the string constants there and rebuild.

Reasons for keeping this static (rather than fetching from Firestore):

- Legal text should be immutable within a shipped version.
- Play Store terms require the policy to be reachable offline.
- No admin dashboard clutter for text that changes maybe once a year.

---

## 5. Callback

Only one — `onBack()` → `navController.popBackStack()`.

---

## 6. Non-obvious details

- **Deep-linking to a specific document is possible** because the route has a
  serialisable `documentId`. Not currently used by the app itself, but future
  push notifications could point to a legal update this way.
