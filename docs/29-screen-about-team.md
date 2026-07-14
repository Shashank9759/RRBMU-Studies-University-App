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

# 29 — Screen: About / Team

**File:** `presentation/more/AboutTeamScreen.kt`

The "About the app" page. Also serves as the entry point to legal documents
and to the standalone admin login screen.

---

## 1. Route

```kotlin
@Serializable
data object AboutRoute
```

Reached from the More tab or from Settings (Legal section links).

---

## 2. What it displays

Top to bottom:

1. **Top bar** — back button + "About".
2. **Hero section**:
   - University logo (`BrandLogo`).
   - App name (`AboutDefaults.APP_NAME`).
   - App version (from `BuildConfig.VERSION_NAME` on Android; hardcoded on iOS).
3. **Team members grid** — 2-column grid of `TeamMemberCard`s.
   Data from `AboutDefaults.teamMembers`.
4. **"Contact us" row** — three big buttons:
   - **WhatsApp** → `openUrl("https://wa.me/${AboutDefaults.WHATSAPP_E164}")`
   - **Email** → `openUrl("mailto:${AboutDefaults.EMAIL}")`
   - **Phone** → `openUrl("tel:${AboutDefaults.PHONE_TEL}")`
5. **"Legal" section** — three list rows:
   - **Privacy Policy** → `LegalDocumentRoute("privacy")`
   - **Terms of Service** → `LegalDocumentRoute("terms")`
   - **Disclaimer** → `LegalDocumentRoute("disclaimer")`
6. **Admin badge** — small "Admin" pill at the top when signed in
   (`LocalIsAdmin.current`).

---

## 3. State

No ViewModel. It reads static constants from `AboutDefaults` and reactively
observes `LocalIsAdmin`.

---

## 4. Callbacks (from `AppNavHost`)

```kotlin
AboutTeamScreen(
    onBack = { navController.popBackStack() },
    onOpenLegal = { id -> navController.navigate(LegalDocumentRoute(id)) },
    onContactClick = { type ->
        when (type) {
            ContactType.WhatsApp -> openUrl("https://wa.me/${AboutDefaults.WHATSAPP_E164}")
            ContactType.Email    -> openUrl("mailto:${AboutDefaults.EMAIL}")
            ContactType.Phone    -> openUrl("tel:${AboutDefaults.PHONE_TEL}")
        }
    },
)
```

`ContactType` is a simple enum inside `presentation/more`:
`ContactType { WhatsApp, Email, Phone }`.

---

## 5. Team data

`AboutDefaults.teamMembers` is a `List<TeamMember>`. Each entry has a
`name`, `role`, `photoUrl`, and optional social links (LinkedIn / X / GitHub).
Coil loads the photo via `photoUrl`.

To update: edit `AboutDefaults.kt` — no admin dashboard hooks in for this.

---

## 6. Non-obvious details

- **`openUrl(url)` is platform-specific.** On Android it uses Custom Tabs
  for http/https and a plain `Intent.ACTION_VIEW` for `mailto:`/`tel:`/
  `whatsapp:`. On iOS it uses `UIApplication.shared.open(url)`.
- **Admin login is *not* here.** Admins go through Settings → "Sign in".
  This screen is deliberately student-facing.
