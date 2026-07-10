package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.domain.repository.AnalyticsRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

class FirebaseAnalyticsRepository : AnalyticsRepository {

    override fun logPaperView(paperId: String, courseId: String) {
        Firebase.analytics.logEvent("paper_view") {
            param("paper_id", paperId)
            param("course_id", courseId)
        }
    }

    override fun logDownload(paperId: String, courseId: String) {
        Firebase.analytics.logEvent("download") {
            param("paper_id", paperId)
            param("course_id", courseId)
        }
    }

    override fun logSearch(query: String, resultCount: Int) {
        Firebase.analytics.logEvent("search") {
            param("search_term", query.take(100))
            param("result_count", resultCount.toLong())
        }
    }
}
