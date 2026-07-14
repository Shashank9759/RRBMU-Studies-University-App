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

# RRBMU Studies University App — Full Technical Documentation

Welcome. This folder holds the complete, sequential technical documentation for the
RRBMU Studies University App. Read the files in order — each one builds on the last.

The docs are written in simple language for an active Android / KMP / CMP developer
who wants to understand *every* moving part of this codebase.

---

## How to read these docs

1. Start with **01-project-overview.md** to understand what the app does.
2. Read **02** to **05** to learn the tech stack, folder layout and build.
3. Read **06** to **10** to understand the code layers (domain, data, backend, DI).
4. Read **11** to **13** to understand navigation, theme and shared UI components.
5. From **14** onwards, each file documents **one screen** in the order the user meets
   them. If you only care about one page, jump directly to it.
6. Files **33** to **39** cover the cross-cutting concerns (ads, push, analytics,
   build & run, glossary).

---

## Table of contents

| #  | File | What it covers |
|----|------|----------------|
| 00 | INDEX.md | This file |
| 01 | 01-project-overview.md | What the app is, who uses it, the big picture |
| 02 | 02-tech-stack.md | Every library, framework, tool and *why* it is used |
| 03 | 03-project-structure.md | Modules, folders and where things live |
| 04 | 04-gradle-build-config.md | `build.gradle.kts`, `libs.versions.toml`, Gradle plugins |
| 05 | 05-architecture.md | Clean architecture + MVVM + KMP structure explained |
| 06 | 06-domain-layer.md | Models, repository interfaces, use cases |
| 07 | 07-data-layer.md | Repository implementations, DTOs, mappers, data sources |
| 08 | 08-firebase-backend.md | Firestore schema, Storage paths, security rules, Cloud Functions |
| 09 | 09-local-storage.md | SQLDelight database + multiplatform Settings |
| 10 | 10-dependency-injection.md | Koin setup and every module |
| 11 | 11-navigation.md | Routes, `AppNavHost`, deep links, PDF encoding |
| 12 | 12-theme.md | Colors, typography, dimensions, dark/light mode |
| 13 | 13-ui-components.md | Every reusable Compose component in the app |
| 14 | 14-screen-splash.md | Splash screen |
| 15 | 15-screen-main.md | Main scaffold with 4 bottom tabs |
| 16 | 16-screen-home.md | Home tab |
| 17 | 17-screen-courses.md | Courses tab |
| 18 | 18-screen-course-detail.md | Course detail screen |
| 19 | 19-screen-part-papers.md | Papers for a part |
| 20 | 20-screen-paper-detail.md | Single paper page |
| 21 | 21-screen-pdf-viewer.md | Full-screen PDF viewer |
| 22 | 22-screen-search.md | Global search |
| 23 | 23-screen-notifications.md | Notifications tab |
| 24 | 24-screen-notification-detail.md | Single notification |
| 25 | 25-screen-more.md | More tab |
| 26 | 26-screen-settings.md | Settings screen |
| 27 | 27-screen-downloaded-papers.md | Offline vault |
| 28 | 28-screen-feedback.md | Feedback form |
| 29 | 29-screen-about-team.md | About + team + contact |
| 30 | 30-screen-legal-document.md | Legal document viewer |
| 31 | 31-screen-webview.md | Embedded web view |
| 32 | 32-screens-admin.md | All admin screens (login, dashboard, manage, upload) |
| 33 | 33-ads-admob.md | AdMob integration and ad gating |
| 34 | 34-fcm-push-notifications.md | Firebase Cloud Messaging pipeline |
| 35 | 35-analytics.md | Analytics events |
| 36 | 36-platform-specific.md | `expect` / `actual` implementations on Android and iOS |
| 37 | 37-data-flows.md | End-to-end sequence diagrams of key flows |
| 38 | 38-build-run-deploy.md | Build, run, publish for Android and iOS |
| 39 | 39-glossary.md | Every acronym and term used in the app |

---

## Conventions in these docs

- File paths are relative to the project root, e.g.
  `composeApp/src/commonMain/kotlin/.../HomeScreen.kt`.
- Kotlin code snippets are shown exactly as they appear in the codebase.
- Where a class has many members, we list the *important* ones — not every line.
- "KMP" = Kotlin Multiplatform, "CMP" = Compose Multiplatform.
- The app targets **Android** and **iOS** from a single Kotlin codebase.
