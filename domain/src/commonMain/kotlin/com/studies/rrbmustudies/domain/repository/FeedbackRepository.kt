package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.Feedback

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Result<Unit>
}
