package com.studies.rrbmustudies.presentation.admin

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ImagePickerButton(
    label: String,
    onImagePicked: (PickedFile) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    OutlinedButton(onClick = {}, modifier = modifier, enabled = false) {
        Text("$label (Android only)")
    }
}
