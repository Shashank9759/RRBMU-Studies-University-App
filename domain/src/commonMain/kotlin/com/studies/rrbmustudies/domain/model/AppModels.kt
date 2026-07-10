package com.studies.rrbmustudies.domain.model

data class HomeAd(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String? = null,
    val linkUrl: String,
    val order: Int,
    val isActive: Boolean = true,
    val createdAt: Long = 0,
)

/** Global home carousel timing — same interval for every slide. */
data class HomeCarouselSettings(
    val slideIntervalSeconds: Int = 4,
)

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val linkUrl: String? = null,
    val pageUrl: String? = null,
    /** Optional PDF / image / file URL attached by admin. */
    val attachmentUrl: String? = null,
    val category: NotificationCategory = NotificationCategory.ANNOUNCEMENT,
    val isActive: Boolean = true,
    val createdAt: Long = 0,
) {
    val hasAttachment: Boolean
        get() = !attachmentUrl.isNullOrBlank() ||
            !linkUrl.isNullOrBlank() ||
            !pageUrl.isNullOrBlank() ||
            !imageUrl.isNullOrBlank()

    val resolvedAttachmentUrl: String?
        get() = attachmentUrl?.takeIf { it.isNotBlank() }
            ?: linkUrl?.takeIf { it.isNotBlank() }
            ?: pageUrl?.takeIf { it.isNotBlank() }
            ?: imageUrl?.takeIf { it.isNotBlank() }
}

data class Feedback(
    val id: String = "",
    val rating: Int,
    val comment: String,
    val tags: List<String> = emptyList(),
    val deviceInfo: String? = null,
    val createdAt: Long = 0,
)

data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
)
