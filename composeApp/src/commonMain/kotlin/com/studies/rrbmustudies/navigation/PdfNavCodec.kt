package com.studies.rrbmustudies.navigation

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Nav routes break on raw Firebase URLs (query `?` / `&`).
 * We store a URL-safe Base64 payload instead.
 */
@OptIn(ExperimentalEncodingApi::class)
object PdfNavCodec {
    fun encode(urlOrPath: String): String =
        Base64.UrlSafe.encode(urlOrPath.encodeToByteArray())

    fun decode(encoded: String): String =
        Base64.UrlSafe.decode(encoded).decodeToString()
}
