package com.studies.rrbmustudies.data.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private class IosPdfVaultFileStore : PdfVaultFileStore {

    private val vaultDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true,
            )
            val docs = paths.firstOrNull() as? String
                ?: error("Documents directory unavailable")
            val dir = "$docs/pdf_vault"
            val fm = NSFileManager.defaultManager
            if (!fm.fileExistsAtPath(dir)) {
                fm.createDirectoryAtPath(
                    dir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }
            return dir
        }

    override fun vaultDirectoryPath(): String = vaultDir

    override fun filePathFor(paperId: String): String {
        val safeId = paperId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return "$vaultDir/$safeId.pdf"
    }

    override fun exists(absolutePath: String): Boolean =
        NSFileManager.defaultManager.fileExistsAtPath(absolutePath)

    override fun delete(absolutePath: String): Boolean {
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(absolutePath)) return true
        return fm.removeItemAtPath(absolutePath, error = null)
    }

    override fun deleteAllVaultFiles(): Int {
        val fm = NSFileManager.defaultManager
        val contents = fm.contentsOfDirectoryAtPath(vaultDir, error = null) ?: return 0
        var deleted = 0
        contents.forEach { name ->
            val path = "$vaultDir/$name"
            if (fm.removeItemAtPath(path, error = null)) deleted++
        }
        return deleted
    }

    override fun writeBytes(absolutePath: String, bytes: ByteArray) {
        bytes.usePinned { pinned ->
            val data = NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            data.writeToFile(absolutePath, atomically = true)
        }
    }

    override fun fileSizeBytes(absolutePath: String): Long {
        val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(absolutePath, error = null)
            ?: return 0L
        return (attrs["NSFileSize"] as? Number)?.toLong() ?: 0L
    }
}

actual fun createPdfVaultFileStore(): PdfVaultFileStore = IosPdfVaultFileStore()

@OptIn(ExperimentalForeignApi::class)
actual suspend fun downloadUrlToBytes(url: String): ByteArray = withContext(Dispatchers.Default) {
    val nsUrl = NSURL.URLWithString(url) ?: error("Invalid URL")
    val data = NSData.dataWithContentsOfURL(nsUrl) ?: error("Download failed")
    val length = data.length.toInt()
    ByteArray(length).also { out ->
        out.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
}
