package com.studies.rrbmustudies.data.remote

data class PaperPathIds(
    val courseId: String,
    val systemId: String,
    val partId: String,
    val paperId: String,
)

/**
 * Parses Firestore collection-group document paths of the shape
 * `courses/{courseId}/systems/{systemId}/parts/{partId}/papers/{paperId}`.
 *
 * Depending on platform/SDK the reference path may carry a
 * `projects/{p}/databases/{db}/documents/` prefix, so we anchor on the
 * `courses` segment instead of assuming it is first.
 */
fun parsePaperDocumentPath(path: String): PaperPathIds? {
    val segments = path.trim('/').split('/')
    val start = segments.indexOf("courses")
    if (start == -1 || segments.size < start + 8) return null
    if (segments[start + 2] != "systems" ||
        segments[start + 4] != "parts" ||
        segments[start + 6] != "papers"
    ) {
        return null
    }
    return PaperPathIds(
        courseId = segments[start + 1],
        systemId = segments[start + 3],
        partId = segments[start + 5],
        paperId = segments[start + 7],
    )
}
