package com.studies.rrbmustudies.data.local

import com.studies.rrbmustudies.domain.model.Paper
import kotlinx.coroutines.flow.Flow

interface PaperLocalDataSource {
    fun observeCachedPapers(limit: Int): Flow<List<Paper>>
    suspend fun cachePaper(paper: Paper)
    suspend fun trimCache(maxEntries: Int)
}
