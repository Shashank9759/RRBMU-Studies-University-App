package com.studies.rrbmustudies.presentation.admin

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PdfPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier,
) {
    Button(onClick = {}, modifier = modifier, enabled = false) {
        Text("$label (Android only)")
    }
}
