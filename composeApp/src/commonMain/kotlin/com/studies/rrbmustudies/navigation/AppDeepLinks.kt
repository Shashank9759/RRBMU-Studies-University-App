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

    fun notification(notificationId: String): String =
        "$SCHEME://$HOST_NOTIFICATION/$notificationId"

    fun paper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): String = "$SCHEME://$HOST_PAPER/$courseId/$systemId/$partId/$paperId"

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
    if (!scheme.equals(AppDeepLinks.SCHEME, ignoreCase = true)) return null
    return when (host?.lowercase()) {
        AppDeepLinks.HOST_NOTIFICATION -> {
            val id = pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
            IncomingDeepLink.Notification(id)
        }
        AppDeepLinks.HOST_PAPER -> {
            if (pathSegments.size < 4) return null
            IncomingDeepLink.Paper(
                courseId = pathSegments[0],
                systemId = pathSegments[1],
                partId = pathSegments[2],
                paperId = pathSegments[3],
            )
        }
        else -> null
    }
}
