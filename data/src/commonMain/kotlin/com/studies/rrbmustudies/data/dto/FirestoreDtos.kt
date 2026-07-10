package com.studies.rrbmustudies.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val name: String = "",
    val shortName: String = "",
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val order: Int = 0,
    val isActive: Boolean = true,
    val level: String = "UG",
    val durationYears: Int = 3,
)

@Serializable
data class SystemDto(
    val name: String = "",
    val type: String = "YEARLY",
)

@Serializable
data class PartDto(
    val name: String = "",
    val order: Int = 0,
    val paperCount: Int = 0,
    val description: String? = null,
)

@Serializable
data class PaperDto(
    val title: String = "",
    val subject: String = "",
    val paperCode: String = "",
    val year: Int = 0,
    val description: String? = null,
    val pdfUrl: String = "",
    val coverImageUrl: String? = null,
    val downloadCount: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val createdBy: String? = null,
)

@Serializable
data class HomeAdDto(
    val imageUrl: String = "",
    val title: String = "",
    val description: String? = null,
    val linkUrl: String = "",
    val order: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0,
)

@Serializable
data class HomeCarouselSettingsDto(
    val slideIntervalSeconds: Int = 4,
)

@Serializable
data class NotificationDto(
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val linkUrl: String? = null,
    @SerialName("pageUrl") val pageUrl: String? = null,
    val attachmentUrl: String? = null,
    val category: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = 0,
)

@Serializable
data class FeedbackDto(
    val rating: Int = 0,
    val comment: String = "",
    val tags: List<String> = emptyList(),
    val deviceInfo: String? = null,
    val createdAt: Long = 0,
)

@Serializable
data class AdminDto(
    val email: String = "",
    val displayName: String? = null,
    val role: String? = null,
)
