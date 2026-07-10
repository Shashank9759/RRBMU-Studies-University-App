package com.studies.rrbmustudies.presentation.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.theme.AboutDefaults
import com.studies.rrbmustudies.ui.theme.RrbmuDimens

@Composable
fun LegalDocumentScreen(
    documentId: String,
    onBack: () -> Unit,
) {
    val doc = AboutDefaults.legalDocuments.find { it.id == documentId }
    Scaffold(
        topBar = {
            RrbmuTopBar(showBack = true, onBack = onBack)
        },
    ) { padding ->
        Text(
            text = doc?.body ?: "Document not found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RrbmuDimens.screenHorizontal, vertical = RrbmuDimens.spacingMd),
        )
    }
}
