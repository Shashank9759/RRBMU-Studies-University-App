package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.Paper
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchPapers(query: String, limit: Int = 50): Flow<List<Paper>>
}
