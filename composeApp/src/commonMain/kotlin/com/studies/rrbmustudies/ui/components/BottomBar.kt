package com.studies.rrbmustudies.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.navigation.BottomTab
import com.studies.rrbmustudies.ui.theme.SaffronBrush

@Composable
fun RrbmuBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(20.dp, RoundedCornerShape(999.dp), spotColor = Color(0xFF0A1046))
            .clip(RoundedCornerShape(999.dp))
            .background(if (dark) Color(0xFF171A2B) else Color.White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomTab.entries.forEach { tab ->
            BottomBarItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (tab) {
        BottomTab.Home -> "Home"
        BottomTab.Courses -> "Courses"
        BottomTab.Notifications -> "Alerts"
        BottomTab.More -> "More"
    }
    val icon = when (tab) {
        BottomTab.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        BottomTab.Courses -> if (selected) Icons.Filled.MenuBook else Icons.Outlined.MenuBook
        BottomTab.Notifications -> if (selected) Icons.Filled.Notifications else Icons.Outlined.Notifications
        BottomTab.More -> if (selected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz
    }

    val contentColor by animateColorAsState(
        if (selected) Color.White
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        label = "navColor",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (selected) Modifier.background(SaffronBrush) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
        )
    }
}
