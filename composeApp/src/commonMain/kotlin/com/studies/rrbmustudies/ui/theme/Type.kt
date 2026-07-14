package com.studies.rrbmustudies.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.studies.rrbmustudies.resources.Res
import com.studies.rrbmustudies.resources.fraunces
import com.studies.rrbmustudies.resources.inter
import com.studies.rrbmustudies.resources.sora
import org.jetbrains.compose.resources.Font

// Tri-font strategy (Academic Heritage):
//  - Fraunces  → heritage/editorial hero moments (serif)
//  - Sora      → functional headings, labels, navigation (geometric sans)
//  - Inter     → dense body & utility text
@Composable
fun FrauncesFamily(): FontFamily = FontFamily(
    Font(Res.font.fraunces, FontWeight.Normal),
    Font(Res.font.fraunces, FontWeight.Medium),
    Font(Res.font.fraunces, FontWeight.SemiBold),
    Font(Res.font.fraunces, FontWeight.Bold),
)

@Composable
fun SoraFamily(): FontFamily = FontFamily(
    Font(Res.font.sora, FontWeight.Normal),
    Font(Res.font.sora, FontWeight.Medium),
    Font(Res.font.sora, FontWeight.SemiBold),
    Font(Res.font.sora, FontWeight.Bold),
    Font(Res.font.sora, FontWeight.ExtraBold),
)

@Composable
fun InterFamily(): FontFamily = FontFamily(
    Font(Res.font.inter, FontWeight.Normal),
    Font(Res.font.inter, FontWeight.Medium),
    Font(Res.font.inter, FontWeight.SemiBold),
    Font(Res.font.inter, FontWeight.Bold),
)

@Composable
fun rrbmuTypography(): Typography {
    val heritage = FrauncesFamily()
    val sora = SoraFamily()
    val inter = InterFamily()

    return Typography(
        // Heritage hero moments
        displayLarge = TextStyle(
            fontFamily = heritage, fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.02).em,
        ),
        displayMedium = TextStyle(
            fontFamily = heritage, fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em,
        ),
        displaySmall = TextStyle(
            fontFamily = heritage, fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp, lineHeight = 34.sp, letterSpacing = (-0.01).em,
        ),
        // Functional headings (Sora)
        headlineLarge = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.Bold,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em,
        ),
        headlineMedium = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.Bold,
            fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.01).em,
        ),
        headlineSmall = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.Bold,
            fontSize = 22.sp, lineHeight = 28.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp, lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 22.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp,
        ),
        // Body (Inter)
        bodyLarge = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = inter, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp,
        ),
        // Labels / overlines (Sora)
        labelLarge = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.04.em,
        ),
        labelSmall = TextStyle(
            fontFamily = sora, fontWeight = FontWeight.Bold,
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.05.em,
        ),
    )
}
