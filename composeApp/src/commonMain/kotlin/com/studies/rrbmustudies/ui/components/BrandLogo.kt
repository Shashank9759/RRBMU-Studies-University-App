package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import rrbmustudies.composeapp.generated.resources.Res
import rrbmustudies.composeapp.generated.resources.univ_logo

@Composable
fun BrandLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            // Always white so the seal's transparent corners never blend into a dark top bar.
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.univ_logo),
            contentDescription = "RRBMU logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.92f)
                .padding(1.dp),
        )
    }
}
