package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.studies.rrbmustudies.ui.theme.RrbmuDimens

@Composable
fun AdminFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Admin action",
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = RrbmuDimens.spacingSm, bottom = RrbmuDimens.spacingSm),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Default.Add, contentDescription = contentDescription)
    }
}
