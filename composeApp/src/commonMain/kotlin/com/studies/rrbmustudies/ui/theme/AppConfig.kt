package com.studies.rrbmustudies.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.studies.rrbmustudies.domain.model.ThemeMode

val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }

val UsefulLinks = listOf(
    WebLink("Admit Card / Result", "https://www.univindia.org/ALWARUNIV/"),
    WebLink("University ERP", "https://erp.univindia.org/login"),
    WebLink("Useful Sites", "https://sites.google.com/view/rrusefulsites"),
)

data class WebLink(
    val title: String,
    val url: String,
)
