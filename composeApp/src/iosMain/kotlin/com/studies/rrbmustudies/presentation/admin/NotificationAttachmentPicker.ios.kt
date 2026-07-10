package com.studies.rrbmustudies.presentation.admin

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun NotificationAttachmentPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    OutlinedButton(onClick = {}, modifier = modifier, enabled = false) {
        Text("$label (Android only)")
    }
}
