package com.studies.rrbmustudies.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ads.AdMobBanner
import com.studies.rrbmustudies.ui.theme.LocalIsAdmin

@Composable
fun AdBannerSlot(modifier: Modifier = Modifier) {
    if (LocalIsAdmin.current) return
    AdMobBanner(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
