package com.studies.rrbmustudies.data.platform

/**
 * App-private PDF vault file I/O.
 * Android: filesDir/pdf_vault — never MediaStore / public Downloads.
 */
interface PdfVaultFileStore {
    /** Absolute path of the vault directory (created if needed). */
    fun vaultDirectoryPath(): String

    /** Absolute path for a paper file inside the vault. */
    fun filePathFor(paperId: String): String

    fun exists(absolutePath: String): Boolean

    fun delete(absolutePath: String): Boolean

    fun deleteAllVaultFiles(): Int

    fun writeBytes(absolutePath: String, bytes: ByteArray)

    fun fileSizeBytes(absolutePath: String): Long
}

expect fun createPdfVaultFileStore(): PdfVaultFileStore

expect suspend fun downloadUrlToBytes(url: String): ByteArray
