package com.studies.rrbmustudies.presentation.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun NotificationAttachmentPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
        context.contentResolver.openInputStream(uri)?.use { stream ->
            onFilePicked(PickedFile(stream.readBytes(), name))
        }
    }

    OutlinedButton(
        onClick = {
            launcher.launch(
                arrayOf(
                    "application/pdf",
                    "image/*",
                ),
            )
        },
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(label)
    }
}
