package com.studies.rrbmustudies.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studies.rrbmustudies.ui.components.MoreMenuRow
import com.studies.rrbmustudies.ui.components.RrbmuTopBar
import com.studies.rrbmustudies.ui.components.TeamMemberCard
import com.studies.rrbmustudies.ui.theme.AboutDefaults
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.StitchPrimaryContainer
import com.studies.rrbmustudies.ui.theme.StitchTertiaryContainer

@Composable
fun AboutTeamScreen(
    onBack: () -> Unit,
    onOpenLegal: (documentId: String) -> Unit,
    onContactClick: (ContactType) -> Unit,
) {
    Scaffold(
        topBar = {
            RrbmuTopBar(
                showBack = true,
                onBack = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            item {
                AboutHeroSection()
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RrbmuDimens.screenHorizontal, vertical = RrbmuDimens.spacingLg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "The Team",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = RrbmuDimens.spacingMd),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
            }

            items(AboutDefaults.teamMembers) { member ->
                TeamMemberCard(
                    name = member.name,
                    role = member.role,
                    imageUrl = member.imageUrl,
                    modifier = Modifier.padding(horizontal = RrbmuDimens.screenHorizontal, vertical = 6.dp),
                )
            }

            item {
                SectionBlock(title = "Legal & Info") {
                    AboutDefaults.legalDocuments.forEachIndexed { index, doc ->
                        val icon = when (doc.id) {
                            "disclaimer" -> Icons.Default.Gavel
                            "faq" -> Icons.Default.HelpCenter
                            "privacy" -> Icons.Default.Policy
                            else -> Icons.Default.Description
                        }
                        MoreMenuRow(
                            title = doc.title,
                            icon = icon,
                            onClick = { onOpenLegal(doc.id) },
                        )
                        if (index < AboutDefaults.legalDocuments.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        }
                    }
                }
            }

            item {
                SectionBlock(title = "Contact Us") {
                    MoreMenuRow(
                        title = "WhatsApp",
                        subtitle = AboutDefaults.WHATSAPP,
                        icon = Icons.Default.Chat,
                        onClick = { onContactClick(ContactType.WhatsApp) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    MoreMenuRow(
                        title = "Email",
                        subtitle = AboutDefaults.EMAIL,
                        icon = Icons.Default.Mail,
                        onClick = { onContactClick(ContactType.Email) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    MoreMenuRow(
                        title = "Phone",
                        subtitle = AboutDefaults.PHONE,
                        icon = Icons.Default.Call,
                        onClick = { onContactClick(ContactType.Phone) },
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(RrbmuDimens.spacingXl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Made with ❤️ for RRBMU Students",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                    Text(
                        text = "© 2026 RRBMU Studies. All rights reserved.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutHeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(StitchPrimaryContainer, StitchTertiaryContainer),
                ),
            )
            .padding(vertical = 48.dp, horizontal = RrbmuDimens.spacingLg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingSm),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = StitchPrimaryContainer,
                    modifier = Modifier.size(48.dp),
                )
            }
            Text(
                text = "RRBMU Studies",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "Version ${AboutDefaults.APP_VERSION}",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
            Text(
                text = AboutDefaults.TAGLINE,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(RrbmuDimens.spacingMd))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(RrbmuDimens.spacingMd))
            Text(
                text = AboutDefaults.ABOUT_TEXT,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RrbmuDimens.screenHorizontal, vertical = RrbmuDimens.spacingMd),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = RrbmuDimens.spacingSm),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            content()
        }
    }
}

enum class ContactType {
    WhatsApp,
    Email,
    Phone,
}
