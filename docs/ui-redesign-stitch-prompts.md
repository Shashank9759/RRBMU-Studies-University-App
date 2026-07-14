# RRBMU Studies — Google Stitch UI Redesign Prompts

Design language: **"Scholar" — Editorial Academic × Premium Fintech polish.**
Goal: a distinctive, heritage-meets-modern university papers app. NOT a generic study app.

How to use in Stitch:
1. Paste **PROMPT 0 (Design System)** first. Let Stitch generate the style/tokens.
2. Then generate each screen with its prompt, starting each with: *"Using the same design system, fonts, colors, and components established, design the following screen:"*
3. Keep "Mobile" mode. Generate one screen per prompt for best fidelity.

> Note: Stitch outputs static high-fidelity screens + code, not animations. The **Motion** notes describe how the layout should imply/support motion; actual animations are implemented later in Compose Multiplatform.

---

## PROMPT 0 — MASTER DESIGN SYSTEM

Design a premium mobile design system for **RRBMU Studies**, the official previous-year question-paper app for Raj Rishi Bhartrihari Matsya University, Alwar, Rajasthan. The audience is Indian university students. The feeling should be **prestigious, academic, and heritage-rich, but modern, clean, and premium** — like a blend of a prestigious university's brand, a fintech app's polish (Robinhood/Groww), and an editorial magazine layout. Absolutely avoid a generic, flat, template study-app look.

BRAND & COLOR
- Primary: deep academic indigo `#0A1046` → `#1A237E` (heritage navy, used for text, headers, key surfaces).
- Signature accent: a warm marigold→saffron gradient `#FF9A00` → `#FF5A00` (nods to Rajasthan; used for primary CTAs, active states, highlights).
- Support accent for course color-coding, jewel tones each with its own soft gradient: Royal Violet `#6C5CE7`, Emerald `#0BA678`, Ruby `#E5484D`, Sky `#0EA5E9`, Amber `#F59E0B`, Teal `#14B8A6`.
- Neutrals: warm off-white background `#F7F8FC`, pure-white cards `#FFFFFF`, ink text `#141726`, muted text `#5B6072`, hairline borders `#ECEEF5`.
- Use subtle **mesh / aurora gradients** on hero areas (indigo → violet → saffron), never flat blocks.

TYPOGRAPHY (Google Fonts)
- Display / headings: **Sora** or **Space Grotesk** — geometric, confident, distinctive. Large, bold, tight tracking.
- Body / UI: **Inter** — clean and legible.
- Optional heritage serif accent for the app name and big hero titles: **Fraunces**.
- Strong scale contrast: big bold display numbers/titles vs. small uppercase muted labels with letter-spacing.

SHAPE, DEPTH & SURFACE
- Corner radius: cards 24px, buttons/chips fully rounded (pill), sheets 28px top.
- **Soft layered elevation**: multi-stop shadows, low opacity, large blur — floating, tactile depth (never harsh Material shadows).
- **Glassmorphism** for the top app bar and bottom navigation: frosted translucent blur over content.
- Generous whitespace, 20px screen padding, 16px gaps.
- Subtle geometric watermark motif (a faint university-crest / open-book line pattern) on hero and empty states.

COMPONENTS TO DEFINE
- **Course card**: each course owns a jewel-tone gradient identity. Large abbreviation ("B.Sc") in bold display, full name below, a soft gradient corner glow, a faint crest watermark, a small pill badge (UG / 3 Years). Cards feel like collectible tiles, not boxes.
- **Glass bottom nav**: floating, rounded, frosted; active tab is a saffron pill with an icon that morphs/fills; inactive icons are muted line icons. Tabs: Home, Courses, Alerts, More.
- **Primary button**: saffron gradient, pill, soft glow shadow, bold white label.
- **Secondary/filter chips**: pill; active = indigo fill white text, inactive = white with hairline border.
- **Search bar**: rounded, frosted white, leading search icon, soft inner shadow.
- **Stat / count element**: for "0 Papers", "3 Years" use a small data-viz style with a subtle progress ring or badge.
- **Empty state**: friendly custom line-illustration (open book / inbox), soft indigo tint, encouraging copy.
- **Skeleton loaders**: shimmer placeholders matching card shapes.

MOTION LANGUAGE (design should support these)
- Staggered card entrance (cards rise + fade in sequence).
- Shared-element transition: a course card expands into its detail hero.
- Parallax hero on scroll; frosted bar intensifies on scroll.
- Tactile press: cards scale down 0.97 with a soft glow.
- Animated aurora gradient that slowly drifts on hero.
- Count-up numbers for paper counts.

Deliver a cohesive, unique, premium academic design system with a light theme (and note a dark variant using `#0B0D18` surfaces).

---

## PROMPT 1 — SPLASH

Using the same design system, design a **Splash screen**. Full-bleed animated **aurora mesh gradient** background (indigo → violet → saffron, dark and rich). Centered: the RRBMU circular crest logo inside a soft frosted-glass circle with a subtle glow ring, the wordmark **"RRBMU Studies"** in Fraunces/Sora below in white, and a small tagline "Previous Year Papers • Made Simple". A slim animated saffron progress line at the bottom. Premium, cinematic, confident. Motion: logo scales up with a gentle spring, gradient drifts, progress line fills.

---

## PROMPT 2 — HOME

Using the same design system, design the **Home screen**.
- Frosted glass top bar: left = small RRBMU crest + "RRBMU Studies" wordmark; right = bell icon with a saffron notification dot.
- Rounded frosted **search bar**: "Search papers, subjects, courses…".
- **Hero announcement carousel** (replace any ad look): a premium branded card with an aurora indigo→saffron gradient, a faint crest watermark, a bold headline (e.g. "New: 2026 Papers Live"), a short subline, and a small "Explore" pill. Page-dot indicator below with an elongated active saffron dot. Make it feel like an editorial feature banner, NOT an advertisement.
- Section header "Select Course" with a "View All" saffron text link.
- **Course grid** (2 columns) of collectible course tiles, each with its own jewel-tone gradient identity, big abbreviation (B.Sc, B.A, B.Com, BBA, BCA, B.Ed, BA LLB, BDS), full name, UG badge, and faint crest watermark.
- Section "Recently Added" with a custom empty state (line-art inbox, "No recent papers yet", "Published papers will show here once uploaded.").
- Floating **glass bottom nav** (Home active as saffron pill).
Motion: staggered tile entrance, carousel auto-advance, parallax on scroll.

---

## PROMPT 3 — COURSES LIST

Using the same design system, design the **Courses list screen**.
- Frosted top bar with "RRBMU Studies" + bell.
- Search bar: "Search courses…".
- Horizontal **segmented filter chips**: All (active, indigo), UG, PG, Diploma.
- Small uppercase section label "UNDERGRADUATE" in muted letter-spaced text.
- A clean **list** of course rows: each row has a rounded jewel-tone gradient **avatar** with the abbreviation ("B.S", "B.A"…), the full course name in bold, a row of small pill badges (UG, 3 Years), and a chevron. Rows separated by hairline dividers with soft cards.
- Glass bottom nav, Courses active.
Motion: list rows fade/slide in; chip selection animates the pill.

---

## PROMPT 4 — COURSE DETAIL

Using the same design system, design the **Course Detail screen** for "B.Sc – Bachelor of Science".
- **Fix the broken hero**: a rich full-width **aurora gradient hero header** (this course's jewel tone → indigo) with a faint crest watermark — NO photographic/ad image. Overlaid: a frosted circular back button top-left, small "Back to Courses" label, then a big bold title "B.Sc — Bachelor of Science" and a subtle subtitle "RRBMU • Alwar". Clean layering, strong contrast, nothing overlapping.
- Below hero, three **underlined tabs**: Yearly System (active), Semester System, Entrance Exam — active tab has a saffron underline indicator.
- A vertical list of **Part cards**: each card shows a numbered circular badge (1, 2, 3), "RRBMU Curriculum" small label top-right, bold "Part 1" title, a paper-count stat ("0 Papers" with a small progress ring) + "Previous Year Questions" subtext, and a saffron gradient **"Explore"** pill button.
Motion: hero parallax, tab indicator slides, cards stagger in.

---

## PROMPT 5 — AVAILABLE PAPERS

Using the same design system, design the **Available Papers screen**.
- Frosted top bar with a circular back button + "RRBMU Studies".
- **Breadcrumb** row: B.Sc › Yearly › Part 1 (last crumb bold indigo).
- Big display heading "Available Papers" with a compact "All ▾" filter dropdown pill on the right.
- **Subject filter chips**: "All Subjects" (active indigo), "pathy", etc.
- **Paper cards**: white rounded card with a colored left accent bar, paper title ("Pathology"), a row of small tag pills (CODE: 202, YEAR: 2026, subject), and a circular **download** button with a soft saffron tint on the right. Add a subtle progress/downloaded state design.
- A floating **saffron gradient FAB (+)** bottom-right (admin add) with a soft glow.
Motion: cards stagger in, download button shows a circular progress animation, FAB has a press ripple.

---

## PROMPT 6 — SEARCH

Using the same design system, design a **Search screen**. Focused frosted search field at top with a cancel text button. Below: "Recent searches" as removable pill chips, and "Popular subjects" as a soft grid. When results exist, show result cards (course/subject/paper) with a type badge and jewel-tone accent. Include a friendly empty/no-results state with a line illustration. Clean, fast, premium.

---

## PROMPT 7 — ALERTS (Notifications list + detail)

Using the same design system, design a **Notifications/Alerts screen**.
- List of notification cards: leading circular icon tinted by type (new paper = saffron, announcement = indigo, system = violet), bold title, one-line preview, timestamp, and an unread saffron dot. Unread cards have a faint tinted background.
- A "Mark all read" text action in the header.
- Include the **detail** variant: a full-width tinted header with the icon + title + time, then rich body text in a clean readable column, optional CTA pill.
Empty state: line-art bell, "You're all caught up".

---

## PROMPT 8 — MORE

Using the same design system, design a **More screen**. Top: a profile/brand header card with the RRBMU crest, "RRBMU Studies", and a subtle gradient. Then grouped **settings rows** in soft rounded cards, each with a tinted leading icon, title, and chevron: Feedback, About Team, Settings, Downloaded Papers, Share App, Rate Us, Legal. A small app-version footer. Clean, iOS-settings-grade polish with the academic palette.

---

## PROMPT 9 — SETTINGS

Using the same design system, design a **Settings screen**. Grouped sections in rounded cards: Appearance (Theme: Light/Dark/System segmented control), Notifications (toggles with saffron active track), Downloads (Wi-Fi-only toggle, storage used bar), Data & Cache (clear cache row), About (version). Toggles, segmented controls, and a storage progress bar all styled to the design system.

---

## PROMPT 10 — DOWNLOADED PAPERS

Using the same design system, design a **Downloaded Papers screen**. A list of downloaded paper cards with a PDF file glyph, title, size + date, and a small overflow/delete action; each shows a "Downloaded" checkmark badge. A storage-usage summary bar at top. Empty state: line-art download tray, "No downloads yet".

---

## PROMPT 11 — ABOUT TEAM

Using the same design system, design an **About Team screen**. A hero with the RRBMU crest and a short mission line over an aurora gradient. Then **team member cards**: circular avatar with a gradient ring, name in bold, role in muted text, small social/link icons. A grid or elegant list. Warm, human, premium.

---

## PROMPT 12 — FEEDBACK

Using the same design system, design a **Feedback screen**. A friendly hero line-illustration, a star-rating row (saffron stars), a category chip selector (Bug, Suggestion, Content, Other), a rounded multiline text field, and a saffron gradient "Send Feedback" pill button. Encouraging, delightful, on-brand.

---

## Dark theme note
For every screen, a dark variant uses background `#0B0D18`, cards `#151827`, keeps saffron accents and jewel-tone gradients (slightly desaturated), and text `#EDEEF5`. Glass surfaces become dark-frosted.
