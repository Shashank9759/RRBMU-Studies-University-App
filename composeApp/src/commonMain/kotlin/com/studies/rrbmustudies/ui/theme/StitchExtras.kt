package com.studies.rrbmustudies.ui.theme

import androidx.compose.ui.graphics.Color
import com.studies.rrbmustudies.domain.model.CourseLevel

fun courseAccentColor(shortName: String): Color = when {
    shortName.contains("B.A", ignoreCase = true) -> StitchSecondary
    shortName.contains("B.Com", ignoreCase = true) || shortName.contains("M.Com", ignoreCase = true) -> StitchTertiary
    shortName.contains("BCA", ignoreCase = true) || shortName.contains("MCA", ignoreCase = true) -> StitchOnPrimaryContainer
    shortName.contains("B.Ed", ignoreCase = true) || shortName.contains("M.Ed", ignoreCase = true) -> StitchError
    shortName.contains("BBA", ignoreCase = true) || shortName.contains("MBA", ignoreCase = true) -> StitchPrimaryContainer
    else -> StitchPrimary
}

fun courseBorderAccent(shortName: String): Color = courseAccentColor(shortName).copy(alpha = 0.35f)

fun subjectAccentColor(subject: String): Color {
    val hash = subject.lowercase().hashCode()
    val colors = listOf(SubjectBlue, SubjectIndigo, SubjectEmerald, SubjectAmber, SubjectRose, SubjectSky)
    return colors[kotlin.math.abs(hash) % colors.size]
}

fun levelCategoryLabel(level: CourseLevel): String = when (level) {
    CourseLevel.UG -> "Undergraduate"
    CourseLevel.PG -> "Postgraduate"
    CourseLevel.DIPLOMA -> "Diploma"
}

fun levelDuration(level: CourseLevel): String = when (level) {
    CourseLevel.UG -> "3 Years"
    CourseLevel.PG -> "2 Years"
    CourseLevel.DIPLOMA -> "1–2 Years"
}

fun levelFieldTag(level: CourseLevel): String = when (level) {
    CourseLevel.UG -> "UG"
    CourseLevel.PG -> "PG"
    CourseLevel.DIPLOMA -> "Diploma"
}
