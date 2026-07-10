package com.studies.rrbmustudies.domain.model

enum class NotificationCategory(val label: String) {
    ANNOUNCEMENT("Announcement"),
    TIME_TABLE("Time Table"),
    IMPORTANT("Important"),
    ;

    companion object {
        fun fromRaw(value: String?): NotificationCategory =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: ANNOUNCEMENT
    }
}
