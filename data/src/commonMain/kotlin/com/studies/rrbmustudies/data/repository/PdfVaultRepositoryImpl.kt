package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.data.local.DownloadedPaperLocalDataSource
import com.studies.rrbmustudies.data.platform.PdfVaultFileStore
import com.studies.rrbmustudies.data.platform.downloadUrlToBytes
import com.studies.rrbmustudies.domain.model.DownloadedPaper
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.model.VaultStorageInfo
import com.studies.rrbmustudies.domain.repository.PdfVaultRepository
import com.studies.rrbmustudies.platform.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PdfVaultRepositoryImpl(
    private val local: DownloadedPaperLocalDataSource,
    private val fileStore: PdfVaultFileStore,
) : PdfVaultRepository {

    override fun observeDownloads(): Flow<List<DownloadedPaper>> = local.observeAll()

    override fun observeStorageInfo(): Flow<VaultStorageInfo> = local.observeStorageInfo()

    override fun observeIsDownloaded(paperId: String): Flow<Boolean> =
        local.observeById(paperId).map { entry ->
            entry != null && fileStore.exists(entry.localPath)
        }

    override suspend fun getDownload(paperId: String): DownloadedPaper? {
        val entry = local.getById(paperId) ?: return null
        return if (fileStore.exists(entry.localPath)) entry else null
    }

    override suspend fun downloadToVault(paper: Paper): Result<DownloadedPaper> = runCatching {
        require(paper.pdfUrl.isNotBlank()) { "Paper has no PDF URL" }
        val existing = local.getById(paper.id)
        if (existing != null && fileStore.exists(existing.localPath)) {
            return@runCatching existing
        }

        val bytes = downloadUrlToBytes(paper.pdfUrl)
        require(bytes.isNotEmpty()) { "Downloaded file is empty" }

        val path = fileStore.filePathFor(paper.id)
        withContext(Dispatchers.Default) {
            fileStore.writeBytes(path, bytes)
        }
        val entry = DownloadedPaper(
            id = paper.id,
            courseId = paper.courseId,
            systemId = paper.systemId,
            partId = paper.partId,
            title = paper.title,
            subject = paper.subject,
            paperCode = paper.paperCode,
            year = paper.year,
            remotePdfUrl = paper.pdfUrl,
            localPath = path,
            fileSizeBytes = fileStore.fileSizeBytes(path).takeIf { it > 0 } ?: bytes.size.toLong(),
            downloadedAt = currentTimeMillis(),
        )
        local.upsert(entry)
        entry
    }

    override suspend fun clearVault(): Result<Unit> = runCatching {
        fileStore.deleteAllVaultFiles()
        local.deleteAll()
    }
}
