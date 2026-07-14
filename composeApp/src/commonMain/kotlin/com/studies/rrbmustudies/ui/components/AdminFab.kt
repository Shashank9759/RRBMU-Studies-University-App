package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ui.theme.Saffron
import com.studies.rrbmustudies.ui.theme.SaffronBrush
import com.studies.rrbmustudies.ui.theme.SaffronDeep

@Composable
fun AdminFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Admin action",
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = 20.dp, bottom = 20.dp)
            .size(60.dp)
            .shadow(18.dp, CircleShape, spotColor = SaffronDeep, ambientColor = Saffron)
            .clip(CircleShape)
            .background(SaffronBrush)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}
