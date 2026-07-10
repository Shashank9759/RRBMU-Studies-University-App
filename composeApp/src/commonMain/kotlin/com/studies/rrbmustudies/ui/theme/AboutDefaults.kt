package com.studies.rrbmustudies.ui.theme

data class TeamMember(
    val name: String,
    val role: String,
    val imageUrl: String? = null,
)

data class LegalDocument(
    val id: String,
    val title: String,
    val body: String,
)

object AboutDefaults {
    const val APP_VERSION = "2.0.0"
    const val TAGLINE = "Your University Papers, One Tap Away"
    const val ABOUT_TEXT =
        "RRBMU Studies helps students browse previous year question papers, stay updated with " +
            "university alerts, and study offline — built for RRBMU Alwar students."
    const val UNIVERSITY_NAME = "Raj Rishi Bhartrihari Matsya University, Alwar"
    const val WHATSAPP = "+91 9927904424"
    const val WHATSAPP_E164 = "919927904424"
    const val EMAIL = "rrbmustudies@gmail.com"
    const val PHONE = "+91 9927904424"
    const val PHONE_TEL = "+919927904424"

    val teamMembers = listOf(
        TeamMember(
            name = "Shashank Ranjan",
            role = "Mobile and Multiplatform Engineer",
            imageUrl = null,
        ),
    )

    val legalDocuments = listOf(
        LegalDocument(
            id = "disclaimer",
            title = "Disclaimer",
            body = """
RRBMU Studies is an independently developed educational utility for students of Raj Rishi Bhartrihari Matsya University, Alwar.

This app is not an official product of the university. Question papers, notices, and related content are provided for academic reference only. Always verify important information (admit cards, results, fees, notices) from official university channels.

We aim for accuracy but do not guarantee that every document is complete, current, or error-free. Use of materials is at your own discretion and responsibility.

PDF storage in the app vault is private to your device and is not uploaded to public Downloads unless you choose otherwise.
            """.trimIndent(),
        ),
        LegalDocument(
            id = "faq",
            title = "FAQs",
            body = """
Q: Is this the official university app?
A: No. RRBMU Studies is built independently to help students find previous year papers and alerts in one place.

Q: How do I view a paper online?
A: Open a course → system → part → paper → View Paper. A short loader may appear while the PDF prepares.

Q: What does Download Offline do?
A: It saves the PDF in the app’s private vault so you can open it without internet. It is not placed in your phone’s public Downloads folder.

Q: Why don’t I see papers in some semesters?
A: Some sections are empty until papers are published. Check back later or browse other parts.

Q: How do alerts work?
A: When an admin publishes an active alert, you may receive a system notification. Alerts also appear in the Alerts tab.

Q: How can I contact support?
A: WhatsApp / Phone: +91 9927904424 · Email: rrbmustudies@gmail.com
            """.trimIndent(),
        ),
        LegalDocument(
            id = "privacy",
            title = "Privacy Policy",
            body = """
Last updated: July 2026

RRBMU Studies (“we”, “the app”) respects your privacy.

1. Information we process
• Device identifiers used by Firebase for messaging and analytics (where enabled).
• Feedback you voluntarily submit (rating, comments, optional device info).
• Admin account email/UID if you sign in as an administrator.
• Offline PDFs stored only on your device inside the app vault.

2. How we use information
• Deliver course and paper content from Firebase.
• Send university alerts via Firebase Cloud Messaging when you allow notifications.
• Improve stability and features through aggregated analytics (if enabled).
• Respond to feedback and support requests.

3. Sharing
We do not sell your personal data. Content and infrastructure are hosted on Google Firebase. Third-party processors act under their own terms.

4. Offline vault
Downloaded papers remain on your device until you clear them in Settings. Clearing the app cache may remove temporary online-view files.

5. Your choices
You can deny notification permission, clear offline downloads, and stop using the app at any time.

6. Contact
Email: rrbmustudies@gmail.com
WhatsApp / Phone: +91 9927904424

7. Changes
We may update this policy within the app. Continued use after an update means you accept the revised policy.
            """.trimIndent(),
        ),
        LegalDocument(
            id = "terms",
            title = "Terms of Use",
            body = """
Last updated: July 2026

By using RRBMU Studies you agree to these terms.

1. Educational use
Content is for personal academic study. Do not misuse papers for cheating or commercial redistribution without permission from the rights holders.

2. No official affiliation
The app is not endorsed by the university unless explicitly stated. Official decisions (exams, results, admissions) rest with the university.

3. Accounts
Admin access is restricted to authorized accounts. You must keep credentials secure and not share admin access.

4. Acceptable conduct
Do not attempt to hack, spam, abuse notifications, or upload unlawful or harmful content.

5. Availability
Services may change, be interrupted, or discontinue without notice. We are not liable for downtime or missing documents.

6. Intellectual property
University papers remain subject to their original copyright. App branding and software belong to the developers.

7. Limitation of liability
To the fullest extent allowed by law, the app and developers are not liable for academic consequences, data loss, or damages arising from use of the app.

8. Contact
Questions: rrbmustudies@gmail.com · +91 9927904424

9. Governing approach
These terms are intended for users in India seeking educational convenience. If any part is unenforceable, the rest remains in effect.
            """.trimIndent(),
        ),
    )
}
