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

/** Assign each course a stable jewel-tone identity. Well-known courses get a
 *  curated tone (matching the design mockups); anything else hashes into the
 *  palette so it stays consistent across the app. */
fun courseJewel(shortName: String): JewelTone {
    val key = shortName.uppercase().replace(".", "").replace(" ", "")
    return when {
        key.startsWith("BSC") || key.startsWith("MSC") -> JewelViolet
        key.startsWith("BA") && !key.startsWith("BALLB") -> JewelEmerald
        key.startsWith("BCOM") || key.startsWith("MCOM") -> JewelRuby
        key.startsWith("BCA") || key.startsWith("MCA") -> JewelSky
        key.startsWith("BBA") || key.startsWith("MBA") -> JewelAmber
        key.startsWith("BED") || key.startsWith("MED") -> JewelRose
        key.startsWith("BALLB") || key.contains("LLB") -> JewelIndigo
        key.startsWith("BDS") || key.contains("MBBS") -> JewelTeal
        else -> JewelPalette[kotlin.math.abs(key.hashCode()) % JewelPalette.size]
    }
}

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
