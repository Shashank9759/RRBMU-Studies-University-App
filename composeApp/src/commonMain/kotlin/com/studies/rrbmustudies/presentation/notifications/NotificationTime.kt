package com.studies.rrbmustudies.presentation.notifications

import kotlinx.datetime.Clock

fun formatNotificationTime(createdAt: Long): String {
    if (createdAt <= 0L) return "Recently"
    val diffMs = Clock.System.now().toEpochMilliseconds() - createdAt
    return when {
        diffMs < 60_000 -> "Just now"
        diffMs < 3_600_000 -> "${diffMs / 60_000}m ago"
        diffMs < 86_400_000 -> "${diffMs / 3_600_000}h ago"
        diffMs < 172_800_000 -> "Yesterday"
        diffMs < 604_800_000 -> "${diffMs / 86_400_000} days ago"
        else -> "2 days ago"
    }
}
