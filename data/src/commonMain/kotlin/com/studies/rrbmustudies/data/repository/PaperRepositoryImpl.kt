package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.data.local.PaperLocalDataSource
import com.studies.rrbmustudies.data.mapper.toDomain
import com.studies.rrbmustudies.data.mapper.toDto
import com.studies.rrbmustudies.data.remote.PaperRemoteDataSource
import com.studies.rrbmustudies.data.remote.SearchRemoteDataSource
import com.studies.rrbmustudies.data.remote.StorageRemoteDataSource
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.repository.PaperRepository
import com.studies.rrbmustudies.domain.repository.SearchRepository
import com.studies.rrbmustudies.platform.currentTimeMillis
import com.studies.rrbmustudies.platform.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaperRepositoryImpl(
    private val remote: PaperRemoteDataSource,
    private val storage: StorageRemoteDataSource,
    private val local: PaperLocalDataSource,
) : PaperRepository {

    override fun getPapers(
        courseId: String,
        systemId: String,
        partId: String,
        subjectFilter: String?,
        yearFilter: Int?,
        includeUnpublished: Boolean,
    ): Flow<List<Paper>> =
        remote.observePapers(courseId, systemId, partId, includeUnpublished)
            .map { list ->
                list.map { (id, dto) ->
                    dto.toDomain(id, courseId, systemId, partId)
                }.filter { paper ->
                    (subjectFilter.isNullOrBlank() ||
                        paper.subject.contains(subjectFilter, ignoreCase = true)) &&
                        (yearFilter == null || paper.year == yearFilter)
                }
            }

    override fun getPaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Flow<Paper?> =
        remote.observePaper(courseId, systemId, partId, paperId)
            .map { dto -> dto?.toDomain(paperId, courseId, systemId, partId) }

    override fun getRecentPapers(limit: Int): Flow<List<Paper>> =
        remote.observeRecentPapers(limit).map { list ->
            list.map { ref ->
                ref.dto.toDomain(ref.paperId, ref.courseId, ref.systemId, ref.partId)
            }
        }

    override fun getCachedPapers(limit: Int): Flow<List<Paper>> =
        local.observeCachedPapers(limit)

    override suspend fun cacheViewedPaper(paper: Paper) {
        local.cachePaper(paper)
        local.trimCache(maxEntries = 50)
    }

    override suspend fun incrementDownloadCount(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit> = runCatching {
        remote.incrementDownloadCount(courseId, systemId, partId, paperId)
    }

    override suspend fun uploadPaper(paper: Paper, pdfBytes: ByteArray): Result<Paper> =
        runCatching {
            val now = currentTimeMillis()
            val paperId = paper.id.ifBlank { generateId() }
            val storagePath = "papers/${paper.courseId}/${paper.systemId}/${paper.partId}/$paperId.pdf"
            val pdfUrl = storage.uploadFile(storagePath, pdfBytes, "application/pdf")
            val updated = paper.copy(
                id = paperId,
                pdfUrl = pdfUrl,
                createdAt = if (paper.createdAt == 0L) now else paper.createdAt,
                updatedAt = now,
            )
            remote.savePaper(
                updated.courseId, updated.systemId, updated.partId, paperId, updated.toDto(),
            )
            updated
        }

    override suspend fun updatePaper(paper: Paper): Result<Paper> = runCatching {
        val updated = paper.copy(updatedAt = currentTimeMillis())
        remote.savePaper(
            updated.courseId, updated.systemId, updated.partId, updated.id, updated.toDto(),
        )
        updated
    }

    override suspend fun deletePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Result<Unit> = runCatching {
        remote.deletePaper(courseId, systemId, partId, paperId)
    }
}

class SearchRepositoryImpl(
    private val remote: SearchRemoteDataSource,
) : SearchRepository {

    override fun searchPapers(query: String, limit: Int): Flow<List<Paper>> =
        remote.searchPapers(query, limit).map { list ->
            list.map { ref ->
                ref.dto.toDomain(ref.paperId, ref.courseId, ref.systemId, ref.partId)
            }
        }
}
