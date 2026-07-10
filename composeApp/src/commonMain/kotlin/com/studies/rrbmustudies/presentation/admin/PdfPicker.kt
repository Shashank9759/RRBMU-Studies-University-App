package com.studies.rrbmustudies.presentation.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class PickedFile(
    val bytes: ByteArray,
    val fileName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        return bytes.contentEquals(other.bytes) && fileName == other.fileName
    }

    override fun hashCode(): Int = bytes.contentHashCode() * 31 + fileName.hashCode()
}

@Composable
expect fun PdfPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier = Modifier,
)
