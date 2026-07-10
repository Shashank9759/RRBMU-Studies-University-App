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
actual fun ImagePickerButton(
    label: String,
    onImagePicked: (PickedFile) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "banner.jpg"
        context.contentResolver.openInputStream(uri)?.use { stream ->
            onImagePicked(PickedFile(stream.readBytes(), name))
        }
    }

    OutlinedButton(
        onClick = { launcher.launch("image/*") },
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(label)
    }
}
