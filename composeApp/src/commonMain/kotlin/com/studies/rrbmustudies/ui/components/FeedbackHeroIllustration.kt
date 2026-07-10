package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ui.theme.StitchPrimaryContainer
import com.studies.rrbmustudies.ui.theme.StitchSecondaryContainer
import com.studies.rrbmustudies.ui.theme.StitchTertiaryContainer

/**
 * Offline-friendly brand illustration for Feedback screen.
 * Avoids remote URL loading so the hero always renders.
 */
@Composable
fun FeedbackHeroIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        StitchPrimaryContainer.copy(alpha = 0.28f),
                        StitchTertiaryContainer.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.55f,
                ),
                radius = size.minDimension * 0.48f,
                center = Offset(cx, cy),
            )
            drawCircle(
                color = StitchSecondaryContainer.copy(alpha = 0.18f),
                radius = size.minDimension * 0.12f,
                center = Offset(cx * 0.28f, cy * 0.34f),
            )
            drawCircle(
                color = StitchPrimaryContainer.copy(alpha = 0.16f),
                radius = size.minDimension * 0.09f,
                center = Offset(cx * 1.62f, cy * 0.42f),
            )
            drawCircle(
                color = StitchSecondaryContainer.copy(alpha = 0.14f),
                radius = size.minDimension * 0.07f,
                center = Offset(cx * 1.50f, cy * 1.55f),
            )
        }

        Box(
            modifier = Modifier
                .size(118.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            StitchPrimaryContainer,
                            StitchTertiaryContainer,
                            StitchSecondaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(52.dp),
            )
        }

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = StitchSecondaryContainer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp),
        )
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(26.dp),
        )
    }
}
