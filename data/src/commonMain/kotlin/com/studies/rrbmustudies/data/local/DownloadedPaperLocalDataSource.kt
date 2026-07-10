package com.studies.rrbmustudies.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.studies.rrbmustudies.domain.model.DownloadedPaper
import com.studies.rrbmustudies.domain.model.VaultStorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface DownloadedPaperLocalDataSource {
    fun observeAll(): Flow<List<DownloadedPaper>>
    fun observeStorageInfo(): Flow<VaultStorageInfo>
    fun observeById(paperId: String): Flow<DownloadedPaper?>
    suspend fun getById(paperId: String): DownloadedPaper?
    suspend fun upsert(paper: DownloadedPaper)
    suspend fun deleteById(paperId: String)
    suspend fun deleteAll()
}

class SqlDelightDownloadedPaperLocalDataSource(
    database: RrbmuDatabase,
) : DownloadedPaperLocalDataSource {

    private val queries = database.downloadedPaperQueries

    override fun observeAll(): Flow<List<DownloadedPaper>> =
        queries.selectAllOrdered()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeStorageInfo(): Flow<VaultStorageInfo> =
        combine(
            queries.count().asFlow().mapToOne(Dispatchers.Default),
            queries.totalBytes().asFlow().mapToOne(Dispatchers.Default),
        ) { count, bytes ->
            VaultStorageInfo(count = count.toInt(), totalBytes = bytes)
        }

    override fun observeById(paperId: String): Flow<DownloadedPaper?> =
        queries.selectById(paperId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    override suspend fun getById(paperId: String): DownloadedPaper? = withContext(Dispatchers.Default) {
        queries.selectById(paperId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun upsert(paper: DownloadedPaper) = withContext(Dispatchers.Default) {
        queries.insertOrReplace(
            id = paper.id,
            courseId = paper.courseId,
            systemId = paper.systemId,
            partId = paper.partId,
            title = paper.title,
            subject = paper.subject,
            paperCode = paper.paperCode,
            year = paper.year.toLong(),
            remotePdfUrl = paper.remotePdfUrl,
            localPath = paper.localPath,
            fileSizeBytes = paper.fileSizeBytes,
            downloadedAt = paper.downloadedAt,
        )
    }

    override suspend fun deleteById(paperId: String) = withContext(Dispatchers.Default) {
        queries.deleteById(paperId)
    }

    override suspend fun deleteAll() = withContext(Dispatchers.Default) {
        queries.deleteAll()
    }

    private fun com.studies.rrbmustudies.data.local.DownloadedPaper.toDomain(): DownloadedPaper =
        DownloadedPaper(
            id = id,
            courseId = courseId,
            systemId = systemId,
            partId = partId,
            title = title,
            subject = subject,
            paperCode = paperCode,
            year = year.toInt(),
            remotePdfUrl = remotePdfUrl,
            localPath = localPath,
            fileSizeBytes = fileSizeBytes,
            downloadedAt = downloadedAt,
        )
}
