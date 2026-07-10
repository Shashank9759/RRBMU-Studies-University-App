package com.studies.rrbmustudies.presentation.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NotificationAttachmentPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
