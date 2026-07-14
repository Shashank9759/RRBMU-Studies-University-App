package com.studies.rrbmustudies.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.ui.theme.HeritageIndigo
import com.studies.rrbmustudies.ui.theme.HeritageIndigoDeep
import com.studies.rrbmustudies.ui.theme.HeritageViolet
import com.studies.rrbmustudies.ui.theme.Saffron
import com.studies.rrbmustudies.ui.theme.SaffronBrush
import com.studies.rrbmustudies.ui.theme.SaffronDeep
import com.studies.rrbmustudies.ui.theme.auroraHeroBrush
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

// ---------------------------------------------------------------------------
// Entrance animation — staggered fade + rise, reused across lists/sections.
// ---------------------------------------------------------------------------
@Composable
fun entranceProgress(delayMillis: Int = 0): Float {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        started = true
    }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 480, easing = LinearOutSlowInEasing),
        label = "entrance",
    )
    return progress
}

fun Modifier.entrance(progress: Float, rise: Float = 36f): Modifier = graphicsLayer {
    alpha = progress
    translationY = (1f - progress) * rise
}

// ---------------------------------------------------------------------------
// Saffron CTA — full pill, gradient fill, soft warm glow.
// ---------------------------------------------------------------------------
@Composable
fun SaffronButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (enabled) com.studies.rrbmustudies.ui.theme.rememberPressScale(interaction) else 1f,
        label = "saffronScale",
    )
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .then(
                if (enabled) Modifier.shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(999.dp),
                    spotColor = SaffronDeep,
                    ambientColor = Saffron,
                ) else Modifier,
            )
            .clip(RoundedCornerShape(999.dp))
            .background(SaffronBrush)
            .rippleClickable(interaction) { if (enabled) onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

private fun Modifier.rippleClickable(
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interaction,
    indication = ripple(),
    onClick = onClick,
)

// ---------------------------------------------------------------------------
// Hero announcement carousel — premium branded gradient cards (NOT ad images).
// ---------------------------------------------------------------------------
@Composable
fun HeroAnnouncementCarousel(
    ads: List<HomeAd>,
    onAdClick: (HomeAd) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollIntervalMs: Long = 4000L,
) {
    if (ads.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { ads.size })

    if (ads.size > 1) {
        LaunchedEffect(pagerState) {
            while (true) {
                delay(autoScrollIntervalMs)
                val next = (pagerState.currentPage + 1) % ads.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        ) { page ->
            val ad = ads[page]
            val offset = ((pagerState.currentPage - page) +
                pagerState.currentPageOffsetFraction).absoluteValue
            HeroCard(
                ad = ad,
                onClick = { onAdClick(ad) },
                modifier = Modifier.graphicsLayer {
                    val s = 1f - (offset * 0.06f).coerceIn(0f, 0.06f)
                    scaleX = s; scaleY = s
                    alpha = 1f - (offset * 0.3f).coerceIn(0f, 0.3f)
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(ads.size) { i ->
                val selected = pagerState.currentPage == i
                val w by animateFloatAsState(if (selected) 22f else 6f, label = "dot")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(w.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) SaffronBrush else brushOf(MaterialTheme.colorScheme.outlineVariant)),
                )
            }
        }
    }
}

private fun brushOf(color: Color): Brush = Brush.linearGradient(listOf(color, color))

@Composable
private fun HeroCard(
    ad: HomeAd,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = HeritageIndigo)
            .clip(RoundedCornerShape(24.dp))
            .background(auroraHeroBrush(HeritageViolet))
            .rippleClickable(interaction, onClick)
            .height(190.dp),
    ) {
        if (ad.imageUrl.isNotBlank()) {
            coil3.compose.AsyncImage(
                model = ad.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            // Indigo scrim keeps the overlay text readable over any image.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                HeritageIndigoDeep.copy(alpha = 0.85f),
                                HeritageIndigo.copy(alpha = 0.35f),
                            ),
                        ),
                    ),
            )
        }
        // crest watermark motif
        CrestWatermark(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(150.dp)
                .graphicsLayer { alpha = 0.10f },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "ACADEMIC YEAR 2026",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = ad.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!ad.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ad.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = HeritageIndigoDeep,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small stat ring (paper counts, etc.)
// ---------------------------------------------------------------------------
@Composable
fun StatRing(
    value: String,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .drawWithContent {
                drawContent()
                drawCircle(
                    color = ringColor,
                    radius = size.minDimension / 2f - 1.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Faint geometric crest watermark (open-book / hexagon motif).
// ---------------------------------------------------------------------------
@Composable
fun CrestWatermark(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    Box(
        modifier = modifier.drawWithContent {
            val w = size.width
            val h = size.height
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            // hexagon
            val path = androidx.compose.ui.graphics.Path().apply {
                val cx = w / 2f; val cy = h / 2f; val r = w * 0.38f
                for (i in 0..6) {
                    val a = (kotlin.math.PI / 3f * i - kotlin.math.PI / 2f).toFloat()
                    val x = cx + r * kotlin.math.cos(a)
                    val y = cy + r * kotlin.math.sin(a)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path, color = color, style = stroke)
            drawLine(
                color = color,
                start = Offset(w * 0.5f, h * 0.2f),
                end = Offset(w * 0.5f, h * 0.8f),
                strokeWidth = 2.dp.toPx(),
            )
        },
    )
}
