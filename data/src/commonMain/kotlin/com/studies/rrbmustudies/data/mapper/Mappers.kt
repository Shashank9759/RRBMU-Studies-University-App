package com.studies.rrbmustudies.data.mapper

import com.studies.rrbmustudies.data.dto.AdminDto
import com.studies.rrbmustudies.data.dto.CourseDto
import com.studies.rrbmustudies.data.dto.FeedbackDto
import com.studies.rrbmustudies.data.dto.HomeAdDto
import com.studies.rrbmustudies.data.dto.NotificationDto
import com.studies.rrbmustudies.data.dto.PaperDto
import com.studies.rrbmustudies.data.dto.PartDto
import com.studies.rrbmustudies.data.dto.SystemDto
import com.studies.rrbmustudies.domain.model.AdminUser
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.domain.model.NotificationCategory
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.Feedback
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.Part
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.model.SystemType

fun CourseDto.toDomain(id: String): Course = Course(
    id = id,
    name = name,
    shortName = shortName,
    iconUrl = iconUrl,
    backgroundImageUrl = backgroundImageUrl,
    order = order,
    isActive = isActive,
    level = level.toCourseLevel(),
    durationYears = durationYears.coerceIn(1, 6),
)

fun Course.toDto(): CourseDto = CourseDto(
    name = name,
    shortName = shortName,
    iconUrl = iconUrl,
    backgroundImageUrl = backgroundImageUrl,
    order = order,
    isActive = isActive,
    level = level.name,
    durationYears = durationYears.coerceIn(1, 6),
)

fun SystemDto.toDomain(id: String, courseId: String): CourseSystem = CourseSystem(
    id = id,
    courseId = courseId,
    name = name,
    type = type.toSystemType(),
)

fun CourseSystem.toDto(): SystemDto = SystemDto(
    name = name,
    type = type.name,
)

fun Part.toDto(): PartDto = PartDto(
    name = name,
    order = order,
    paperCount = paperCount,
    description = description,
)

fun PartDto.toDomain(id: String, courseId: String, systemId: String): Part = Part(
    id = id,
    courseId = courseId,
    systemId = systemId,
    name = name,
    order = order,
    paperCount = paperCount,
    description = description?.takeIf { it.isNotBlank() },
)

fun PaperDto.toDomain(
    id: String,
    courseId: String,
    systemId: String,
    partId: String,
): Paper = Paper(
    id = id,
    courseId = courseId,
    systemId = systemId,
    partId = partId,
    title = title,
    subject = subject,
    paperCode = paperCode,
    year = year,
    description = description,
    pdfUrl = pdfUrl,
    coverImageUrl = coverImageUrl,
    downloadCount = downloadCount,
    isPublished = isPublished,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
)

fun Paper.toDto(): PaperDto = PaperDto(
    title = title,
    subject = subject,
    paperCode = paperCode,
    year = year,
    description = description,
    pdfUrl = pdfUrl,
    coverImageUrl = coverImageUrl,
    downloadCount = downloadCount,
    isPublished = isPublished,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdBy = createdBy,
)

fun HomeAdDto.toDomain(id: String): HomeAd = HomeAd(
    id = id,
    imageUrl = imageUrl,
    title = title,
    description = description,
    linkUrl = linkUrl,
    order = order,
    isActive = isActive,
    createdAt = createdAt,
)

fun HomeAd.toDto(): HomeAdDto = HomeAdDto(
    imageUrl = imageUrl,
    title = title,
    description = description,
    linkUrl = linkUrl,
    order = order,
    isActive = isActive,
    createdAt = createdAt,
)

fun NotificationDto.toDomain(id: String): AppNotification = AppNotification(
    id = id,
    title = title,
    body = body,
    imageUrl = imageUrl,
    linkUrl = linkUrl,
    pageUrl = pageUrl,
    attachmentUrl = attachmentUrl,
    category = NotificationCategory.fromRaw(category),
    isActive = isActive,
    createdAt = createdAt,
)

fun AppNotification.toDto(): NotificationDto = NotificationDto(
    title = title,
    body = body,
    imageUrl = imageUrl,
    linkUrl = linkUrl,
    pageUrl = pageUrl,
    attachmentUrl = attachmentUrl,
    category = category.name.lowercase(),
    isActive = isActive,
    createdAt = createdAt,
)

fun Feedback.toDto(): FeedbackDto = FeedbackDto(
    rating = rating,
    comment = comment,
    tags = tags,
    deviceInfo = deviceInfo,
    createdAt = createdAt,
)

fun AdminDto.toDomain(uid: String): AdminUser = AdminUser(
    uid = uid,
    email = email,
    displayName = displayName,
)

private fun String.toCourseLevel(): CourseLevel = when (uppercase()) {
    "PG" -> CourseLevel.PG
    "DIPLOMA" -> CourseLevel.DIPLOMA
    else -> CourseLevel.UG
}

private fun String.toSystemType(): SystemType = when (uppercase()) {
    "SEMESTER" -> SystemType.SEMESTER
    "ENTRANCE" -> SystemType.ENTRANCE
    else -> SystemType.YEARLY
}
