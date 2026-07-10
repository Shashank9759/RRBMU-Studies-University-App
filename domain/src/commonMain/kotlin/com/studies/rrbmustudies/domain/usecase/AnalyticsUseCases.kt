package com.studies.rrbmustudies.domain.usecase

import com.studies.rrbmustudies.domain.repository.AnalyticsRepository

class LogPaperViewUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    operator fun invoke(paperId: String, courseId: String) {
        analyticsRepository.logPaperView(paperId, courseId)
    }
}

class LogDownloadUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    operator fun invoke(paperId: String, courseId: String) {
        analyticsRepository.logDownload(paperId, courseId)
    }
}

class LogSearchUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    operator fun invoke(query: String, resultCount: Int) {
        analyticsRepository.logSearch(query, resultCount)
    }
}
