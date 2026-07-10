package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.Paper
import kotlinx.coroutines.flow.Flow

interface PaperRepository {
    fun getPapers(
        courseId: String,
        systemId: String,
        partId: String,
        subjectFilter: String? = null,
        yearFilter: Int? = null,
        includeUnpublished: Boolean = false,
    ): Flow<List<Paper>>

    fun getPaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Flow<Paper?>

    fun getRecentPapers(limit: Int = 10): Flow<List<Paper>>

    fun getCachedPapers(limit: Int = 20): Flow<List<Paper>>

    suspend fun cacheViewedPaper(paper: Paper)

    suspend fun incrementDownloadCount(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit>

    suspend fun uploadPaper(paper: Paper, pdfBytes: ByteArray): Result<Paper>
    suspend fun updatePaper(paper: Paper): Result<Paper>
    suspend fun deletePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit>
}
