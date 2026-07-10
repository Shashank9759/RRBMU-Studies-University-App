package com.studies.rrbmustudies.presentation.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PdfPickerButton(
    label: String,
    onFilePicked: (PickedFile) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "paper.pdf"
        context.contentResolver.openInputStream(uri)?.use { stream ->
            onFilePicked(PickedFile(stream.readBytes(), name))
        }
    }

    Button(
        onClick = { launcher.launch(arrayOf("application/pdf")) },
        modifier = modifier,
    ) {
        Text(label)
    }
}
