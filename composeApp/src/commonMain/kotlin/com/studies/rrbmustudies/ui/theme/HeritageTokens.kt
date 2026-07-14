package com.studies.rrbmustudies.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Academic Heritage — brand foundation
// ---------------------------------------------------------------------------

// Deep academic indigo
val HeritageIndigo = Color(0xFF0A1046)
val HeritageIndigoBright = Color(0xFF1A237E)
val HeritageIndigoDeep = Color(0xFF070B2E)
val HeritageViolet = Color(0xFF3A2A8C)

// Signature saffron → marigold (CTAs, active states only)
val SaffronLight = Color(0xFFFFB020)
val Saffron = Color(0xFFFF9A00)
val SaffronDeep = Color(0xFFFF5A00)

// Glass
val GlassLightFill = Color(0xCCFFFFFF)
val GlassLightBorder = Color(0x99FFFFFF)
val GlassDarkFill = Color(0x991A1F35)
val GlassDarkBorder = Color(0x33FFFFFF)

// Dark canvas
val HeritageDarkSurface = Color(0xFF0B0D18)
val HeritageDarkCard = Color(0xFF151827)

// ---------------------------------------------------------------------------
// Jewel tones — each is a gradient pair used to give a course its identity.
// ---------------------------------------------------------------------------
data class JewelTone(val start: Color, val end: Color, val glow: Color) {
    val brush: Brush get() = Brush.linearGradient(listOf(start, end))
}

val JewelViolet = JewelTone(Color(0xFF7C6CF5), Color(0xFF6C5CE7), Color(0xFF6C5CE7))
val JewelEmerald = JewelTone(Color(0xFF16A97E), Color(0xFF0B8F6B), Color(0xFF0BA678))
val JewelRuby = JewelTone(Color(0xFFEB5A5F), Color(0xFFD43F45), Color(0xFFE5484D))
val JewelSky = JewelTone(Color(0xFF35A9E6), Color(0xFF0E97D9), Color(0xFF0EA5E9))
val JewelAmber = JewelTone(Color(0xFFF7A93B), Color(0xFFEF8B12), Color(0xFFF59E0B))
val JewelTeal = JewelTone(Color(0xFF1FB6A6), Color(0xFF109C8E), Color(0xFF14B8A6))
val JewelRose = JewelTone(Color(0xFFF15C86), Color(0xFFDD3D6C), Color(0xFFF43F5E))
val JewelIndigo = JewelTone(Color(0xFF5561D6), Color(0xFF3B47B8), Color(0xFF4C56AF))

val JewelPalette: List<JewelTone> = listOf(
    JewelViolet, JewelEmerald, JewelRuby, JewelSky,
    JewelAmber, JewelTeal, JewelRose, JewelIndigo,
)

// ---------------------------------------------------------------------------
// Brushes
// ---------------------------------------------------------------------------
val SaffronBrush: Brush get() = Brush.linearGradient(listOf(Saffron, SaffronDeep))
val SaffronBrushVertical: Brush get() = Brush.verticalGradient(listOf(SaffronLight, SaffronDeep))

/** Aurora indigo→violet mesh used for hero headers. */
fun auroraHeroBrush(tint: Color = HeritageViolet): Brush = Brush.linearGradient(
    colors = listOf(HeritageIndigoDeep, HeritageIndigo, tint),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

/** Course hero: jewel tone bleeding into deep indigo. */
fun courseHeroBrush(jewel: JewelTone): Brush = Brush.linearGradient(
    colors = listOf(HeritageIndigoDeep, HeritageIndigo, jewel.end),
)

// ---------------------------------------------------------------------------
// Modifiers
// ---------------------------------------------------------------------------

/** Frosted glass surface (top bar / bottom nav). Blur is approximated with a
 *  translucent fill + hairline border since true backdrop blur is unavailable
 *  cross-platform. */
fun Modifier.glassSurface(
    dark: Boolean = false,
    radius: Int = 999,
): Modifier = this
    .background(
        color = if (dark) GlassDarkFill else GlassLightFill,
        shape = RoundedCornerShape(radius.dp),
    )

/** Soft, multi-stop ambient elevation used by white cards. */
val AmbientCardShadow = Shadow(
    color = Color(0x140A1046),
    offset = Offset(0f, 8f),
    blurRadius = 24f,
)

/** Press feedback: scales an interactive surface to 0.97 while pressed. */
@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    return if (pressed) 0.97f else 1f
}
