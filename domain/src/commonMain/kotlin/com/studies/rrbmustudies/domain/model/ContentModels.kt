package com.studies.rrbmustudies.domain.model

data class Course(
    val id: String,
    val name: String,
    val shortName: String,
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val order: Int,
    val isActive: Boolean = true,
    val level: CourseLevel,
    /** Typical programme length in years (e.g. 2 → semesters 1–4, 3 → 1–6). */
    val durationYears: Int = 3,
)

data class CourseSystem(
    val id: String,
    val courseId: String,
    val name: String,
    val type: SystemType,
)

data class Part(
    val id: String,
    val courseId: String,
    val systemId: String,
    val name: String,
    val order: Int,
    val paperCount: Int = 0,
    /** Optional subtitle under the part name; hidden when blank. */
    val description: String? = null,
)

data class Paper(
    val id: String,
    val courseId: String,
    val systemId: String,
    val partId: String,
    val title: String,
    val subject: String,
    val paperCode: String,
    val year: Int,
    val description: String? = null,
    val pdfUrl: String,
    val coverImageUrl: String? = null,
    val downloadCount: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val createdBy: String? = null,
)

/** PDF stored in the app-private offline vault. */
data class DownloadedPaper(
    val id: String,
    val courseId: String,
    val systemId: String,
    val partId: String,
    val title: String,
    val subject: String,
    val paperCode: String,
    val year: Int,
    val remotePdfUrl: String,
    val localPath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long,
)

data class VaultStorageInfo(
    val count: Int = 0,
    val totalBytes: Long = 0L,
) {
    val sizeLabel: String
        get() = when {
            totalBytes <= 0L -> "0 MB"
            totalBytes < 1024L * 1024L -> "${(totalBytes + 512) / 1024} KB"
            else -> {
                val mb = totalBytes.toDouble() / (1024.0 * 1024.0)
                val rounded = (mb * 10.0).toInt() / 10.0
                "$rounded MB"
            }
        }
}
