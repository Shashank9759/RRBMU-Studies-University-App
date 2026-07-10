package com.studies.rrbmustudies.data.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AndroidPdfVaultFileStore(
    private val context: Context,
) : PdfVaultFileStore {

    private val vaultDir: File
        get() = File(context.filesDir, "pdf_vault").also { if (!it.exists()) it.mkdirs() }

    override fun vaultDirectoryPath(): String = vaultDir.absolutePath

    override fun filePathFor(paperId: String): String {
        val safeId = paperId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(vaultDir, "$safeId.pdf").absolutePath
    }

    override fun exists(absolutePath: String): Boolean = File(absolutePath).isFile

    override fun delete(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return !file.exists() || file.delete()
    }

    override fun deleteAllVaultFiles(): Int {
        val files = vaultDir.listFiles() ?: return 0
        var deleted = 0
        files.forEach { if (it.isFile && it.delete()) deleted++ }
        return deleted
    }

    override fun writeBytes(absolutePath: String, bytes: ByteArray) {
        File(absolutePath).outputStream().use { it.write(bytes) }
    }

    override fun fileSizeBytes(absolutePath: String): Long {
        val file = File(absolutePath)
        return if (file.isFile) file.length() else 0L
    }
}

actual fun createPdfVaultFileStore(): PdfVaultFileStore {
    error("Use AndroidPdfVaultFileStore via Koin with Context")
}

actual suspend fun downloadUrlToBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 30_000
        readTimeout = 60_000
        requestMethod = "GET"
        instanceFollowRedirects = true
    }
    try {
        val code = connection.responseCode
        if (code !in 200..299) {
            error("Download failed (HTTP $code)")
        }
        connection.inputStream.use { it.readBytes() }
    } finally {
        connection.disconnect()
    }
}
