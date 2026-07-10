package com.studies.rrbmustudies.ui.util

/**
 * Maps technical failures (Firebase, network, I/O) to short user-facing copy.
 * Never pass [Throwable.message] straight into UI.
 */
fun Throwable.toUserMessage(fallback: String): String {
    val raw = (message ?: "").lowercase()
    return when {
        raw.contains("permission") || raw.contains("permission_denied") ||
            raw.contains("insufficient") || raw.contains("unauthenticated") ->
            "You don’t have permission to do that."

        raw.contains("network") || raw.contains("unreachable") ||
            raw.contains("timeout") || raw.contains("unable to resolve") ||
            raw.contains("failed to connect") || raw.contains("offline") ->
            "Check your internet connection and try again."

        raw.contains("user-not-found") || raw.contains("wrong-password") ||
            raw.contains("invalid-credential") || raw.contains("invalid_login") ||
            raw.contains("invalid-email") || raw.contains("auth/") ->
            "Incorrect email or password."

        raw.contains("too-many-requests") ->
            "Too many attempts. Please wait and try again."

        raw.contains("not found") || raw.contains("404") ->
            "We couldn’t find that item."

        raw.contains("storage") || raw.contains("upload") ->
            "Upload failed. Please try again."

        raw.contains("http") && raw.any { it.isDigit() } ->
            fallback

        else -> fallback
    }
}

fun String?.nullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

fun isValidHttpUrl(url: String): Boolean {
    val trimmed = url.trim()
    return trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
}
