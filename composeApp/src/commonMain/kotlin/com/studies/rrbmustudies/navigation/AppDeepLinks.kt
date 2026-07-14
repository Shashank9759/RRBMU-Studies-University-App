package com.studies.rrbmustudies.navigation

/**
 * App-owned share / deep-link URIs.
 *
 * Format:
 * - rrbmustudies://notification/{notificationId}
 * - rrbmustudies://paper/{courseId}/{systemId}/{partId}/{paperId}
 *
 * Tapping these opens RRBMU Studies via the Android VIEW intent-filter.
 */
object AppDeepLinks {
    const val SCHEME = "rrbmustudies"
    const val HOST_NOTIFICATION = "notification"
    const val HOST_PAPER = "paper"

    /** Firebase Hosting domain — https links are clickable in WhatsApp/browsers
     *  and open the app directly via Android App Links. */
    const val WEB_HOST = "rbmu-studies-prod.web.app"

    fun notification(notificationId: String): String =
        "https://$WEB_HOST/$HOST_NOTIFICATION/$notificationId"

    fun paper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): String = "https://$WEB_HOST/$HOST_PAPER/$courseId/$systemId/$partId/$paperId"

    fun shareNotification(title: String, body: String, notificationId: String): String =
        buildString {
            append(title.trim())
            if (body.isNotBlank()) {
                append("\n\n")
                append(body.trim())
            }
            append("\n\nOpen in RRBMU Studies:\n")
            append(notification(notificationId))
        }

    fun sharePaper(
        title: String,
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
        subject: String = "",
        year: String = "",
    ): String = buildString {
        append(title.trim())
        val meta = listOfNotNull(
            subject.takeIf { it.isNotBlank() }?.let { "Subject: $it" },
            year.takeIf { it.isNotBlank() }?.let { "Year: $it" },
        ).joinToString(" · ")
        if (meta.isNotBlank()) {
            append("\n")
            append(meta)
        }
        append("\n\nOpen in RRBMU Studies:\n")
        append(paper(courseId, systemId, partId, paperId))
    }
}

sealed class IncomingDeepLink {
    data class Notification(
        val notificationId: String,
        val openAttachment: Boolean = false,
    ) : IncomingDeepLink()

    data class Paper(
        val courseId: String,
        val systemId: String,
        val partId: String,
        val paperId: String,
    ) : IncomingDeepLink()
}

fun parseAppDeepLink(
    scheme: String?,
    host: String?,
    pathSegments: List<String>,
): IncomingDeepLink? {
    // Two accepted shapes:
    //  - legacy custom scheme: rrbmustudies://paper/{ids...}   (kind = host)
    //  - https App Link:       https://<web-host>/paper/{ids...} (kind = first path segment)
    val isCustomScheme = scheme.equals(AppDeepLinks.SCHEME, ignoreCase = true)
    val isWebLink = scheme.equals("https", ignoreCase = true) &&
        host.equals(AppDeepLinks.WEB_HOST, ignoreCase = true)
    if (!isCustomScheme && !isWebLink) return null

    val kind: String?
    val args: List<String>
    if (isCustomScheme) {
        kind = host?.lowercase()
        args = pathSegments
    } else {
        kind = pathSegments.firstOrNull()?.lowercase()
        args = pathSegments.drop(1)
    }

    return when (kind) {
        AppDeepLinks.HOST_NOTIFICATION -> {
            val id = args.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
            IncomingDeepLink.Notification(id)
        }
        AppDeepLinks.HOST_PAPER -> {
            if (args.size < 4) return null
            IncomingDeepLink.Paper(
                courseId = args[0],
                systemId = args[1],
                partId = args[2],
                paperId = args[3],
            )
        }
        else -> null
    }
}
