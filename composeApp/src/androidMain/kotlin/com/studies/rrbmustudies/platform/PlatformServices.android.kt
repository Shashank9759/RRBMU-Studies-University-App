package com.studies.rrbmustudies.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object AndroidAppContext {
    lateinit var appContext: Context
}

actual fun initializePlatform() {
    // appContext set from MainActivity
}

fun bindAndroidContext(context: Context) {
    AndroidAppContext.appContext = context.applicationContext
}

actual fun openUrl(url: String) {
    val context = AndroidAppContext.appContext
    val parsed = Uri.parse(url)
    // appContext is not an Activity — Custom Tabs / VIEW must carry NEW_TASK.
    val customTabs = CustomTabsIntent.Builder().build()
    customTabs.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        customTabs.launchUrl(context, parsed)
    } catch (_: Exception) {
        val fallback = Intent(Intent.ACTION_VIEW, parsed).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(fallback)
    }
}

actual fun shareText(text: String, title: String) {
    val context = AndroidAppContext.appContext
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(url)
            }
        },
        update = { it.loadUrl(url) },
    )
}

private sealed interface PdfReadyState {
    data object Loading : PdfReadyState
    data class Ready(val localPath: String) : PdfReadyState
    data class Failed(val message: String) : PdfReadyState
}

@Composable
actual fun PlatformPdfViewer(
    pdfUrl: String,
    modifier: Modifier,
) {
    var state by remember(pdfUrl) { mutableStateOf<PdfReadyState>(PdfReadyState.Loading) }
    var retryKey by remember(pdfUrl) { mutableStateOf(0) }

    LaunchedEffect(pdfUrl, retryKey) {
        state = PdfReadyState.Loading
        state = withContext(Dispatchers.IO) {
            runCatching { resolvePdfToLocalFile(AndroidAppContext.appContext, pdfUrl) }
                .fold(
                    onSuccess = { PdfReadyState.Ready(it.absolutePath) },
                    onFailure = { PdfReadyState.Failed(it.message ?: "Couldn't open PDF") },
                )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val current = state) {
            PdfReadyState.Loading -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading PDF…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is PdfReadyState.Failed -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        current.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { retryKey++ }) {
                        Text("Retry")
                    }
                }
            }
            is PdfReadyState.Ready -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PDFView(ctx, null).apply {
                            fromFile(File(current.localPath))
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .defaultPage(0)
                                .spacing(8)
                                .load()
                        }
                    },
                    update = { /* keep loaded file */ },
                )
            }
        }
    }
}

/**
 * AndroidPdfViewer cannot stream HTTPS URLs reliably.
 * Local paths are opened directly; remote URLs are downloaded into a cache file first.
 */
private fun resolvePdfToLocalFile(context: Context, source: String): File {
    val trimmed = source.trim()
    require(trimmed.isNotBlank()) { "PDF source is empty" }

    if (!trimmed.startsWith("http://", ignoreCase = true) &&
        !trimmed.startsWith("https://", ignoreCase = true)
    ) {
        val local = when {
            trimmed.startsWith("file://", ignoreCase = true) -> File(Uri.parse(trimmed).path ?: trimmed)
            else -> File(trimmed)
        }
        require(local.isFile && local.length() > 0L) { "PDF file not found" }
        return local
    }

    val cacheDir = File(context.cacheDir, "pdf_view_cache").also { if (!it.exists()) it.mkdirs() }
    val cacheFile = File(cacheDir, "${sha256Hex(trimmed)}.pdf")
    if (cacheFile.isFile && cacheFile.length() > 0L) {
        return cacheFile
    }

    val connection = (URL(trimmed).openConnection() as HttpURLConnection).apply {
        connectTimeout = 30_000
        readTimeout = 90_000
        requestMethod = "GET"
        instanceFollowRedirects = true
        setRequestProperty("Accept", "application/pdf,*/*")
    }
    try {
        val code = connection.responseCode
        if (code !in 200..299) {
            error("Couldn't download PDF (HTTP $code)")
        }
        val bytes = connection.inputStream.use { it.readBytes() }
        require(bytes.isNotEmpty()) { "Downloaded PDF is empty" }
        val temp = File(cacheDir, "${cacheFile.name}.tmp")
        temp.outputStream().use { it.write(bytes) }
        if (!temp.renameTo(cacheFile)) {
            cacheFile.outputStream().use { it.write(bytes) }
            temp.delete()
        }
        return cacheFile
    } finally {
        connection.disconnect()
    }
}

private fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}
