package com.studies.rrbmustudies.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ui.components.BrandLogo
import com.studies.rrbmustudies.ui.components.CrestWatermark
import com.studies.rrbmustudies.ui.theme.FrauncesFamily
import com.studies.rrbmustudies.ui.theme.HeritageViolet
import com.studies.rrbmustudies.ui.theme.SaffronBrush
import com.studies.rrbmustudies.ui.theme.auroraHeroBrush
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(600),
        label = "logoScale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha",
    )
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1100, easing = LinearEasing),
        label = "progress",
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(auroraHeroBrush(HeritageViolet)),
        contentAlignment = Alignment.Center,
    ) {
        CrestWatermark(
            modifier = Modifier
                .align(Alignment.Center)
                .size(320.dp)
                .graphicsLayer { alpha = 0.06f },
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale; scaleY = logoScale; alpha = logoAlpha
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                BrandLogo(size = 84.dp)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "RRBMU Studies",
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = FrauncesFamily()),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Previous Year Papers • Made Simple",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer { alpha = logoAlpha },
            )
        }
        // Saffron progress line
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(0.4f)
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(SaffronBrush),
            )
        }
    }
}
