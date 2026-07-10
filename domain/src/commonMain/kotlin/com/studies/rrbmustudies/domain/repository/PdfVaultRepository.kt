package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.DownloadedPaper
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.model.VaultStorageInfo
import kotlinx.coroutines.flow.Flow

interface PdfVaultRepository {
    fun observeDownloads(): Flow<List<DownloadedPaper>>
    fun observeStorageInfo(): Flow<VaultStorageInfo>
    fun observeIsDownloaded(paperId: String): Flow<Boolean>
    suspend fun getDownload(paperId: String): DownloadedPaper?
    /** Download remote PDF into app-private storage and persist metadata. */
    suspend fun downloadToVault(paper: Paper): Result<DownloadedPaper>
    suspend fun clearVault(): Result<Unit>
}
