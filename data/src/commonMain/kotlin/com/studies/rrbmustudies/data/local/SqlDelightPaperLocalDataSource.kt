package com.studies.rrbmustudies.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.platform.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightPaperLocalDataSource(
    private val database: RrbmuDatabase,
) : PaperLocalDataSource {

    private val queries = database.cachedPaperQueries

    override fun observeCachedPapers(limit: Int): Flow<List<Paper>> =
        queries.selectAllOrdered(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun cachePaper(paper: Paper) = withContext(Dispatchers.Default) {
        queries.insertOrReplace(
            id = paper.id,
            courseId = paper.courseId,
            systemId = paper.systemId,
            partId = paper.partId,
            title = paper.title,
            subject = paper.subject,
            paperCode = paper.paperCode,
            year = paper.year.toLong(),
            pdfUrl = paper.pdfUrl,
            coverImageUrl = paper.coverImageUrl,
            cachedAt = currentTimeMillis(),
        )
    }

    override suspend fun trimCache(maxEntries: Int) = withContext(Dispatchers.Default) {
        val count = queries.count().executeAsOne()
        if (count <= maxEntries) return@withContext
        val thresholdRow = queries.selectAllOrdered(maxEntries.toLong()).executeAsList().lastOrNull()
            ?: return@withContext
        queries.deleteOlderThan(thresholdRow.cachedAt)
    }

    private fun com.studies.rrbmustudies.data.local.CachedPaper.toDomain(): Paper = Paper(
        id = id,
        courseId = courseId,
        systemId = systemId,
        partId = partId,
        title = title,
        subject = subject,
        paperCode = paperCode,
        year = year.toInt(),
        pdfUrl = pdfUrl,
        coverImageUrl = coverImageUrl,
    )
}
