package com.studies.rrbmustudies.data.remote

data class PaperPathIds(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val paperId: String,
)

/**
 * Parses Firestore collection-group document paths:
 * `courses/{courseId}/systems/{systemId}/parts/{partId}/papers/{paperId}`
 */
fun parsePaperDocumentPath(path: String): PaperPathIds? {
    val segments = path.trim('/').split('/')
    if (segments.size < 8) return null
    if (segments[0] != "courses" || segments[2] != "systems" ||
        segments[4] != "parts" || segments[6] != "papers"
    ) {
        return null
    }
    return PaperPathIds(
        courseId = segments[1],
        systemId = segments[3],
        partId = segments[5],
        paperId = segments[7],
    )
}
