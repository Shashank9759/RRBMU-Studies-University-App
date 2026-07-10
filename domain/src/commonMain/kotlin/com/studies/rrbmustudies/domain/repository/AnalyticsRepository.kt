package com.studies.rrbmustudies.domain.repository

interface AnalyticsRepository {
    fun logPaperView(paperId: String, courseId: String)
    fun logDownload(paperId: String, courseId: String)
    fun logSearch(query: String, resultCount: Int)
}
