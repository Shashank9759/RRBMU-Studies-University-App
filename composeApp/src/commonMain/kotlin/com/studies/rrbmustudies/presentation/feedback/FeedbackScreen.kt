package com.studies.rrbmustudies.presentation.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.ui.components.DefaultEmojiRatings
import com.studies.rrbmustudies.ui.components.DefaultFeedbackTags
import com.studies.rrbmustudies.ui.components.EmojiRatingBar
import com.studies.rrbmustudies.ui.components.FeedbackHeroIllustration
import com.studies.rrbmustudies.ui.components.FeedbackTagChipRow
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import com.studies.rrbmustudies.ui.theme.StitchPrimaryContainer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    viewModel: FeedbackViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isSubmitted) {
        FeedbackSuccessScreen(onBack = onBack)
        return
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = RrbmuDimens.screenHorizontal),
        ) {
            RowHeader(onBack = onBack)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RrbmuDimens.spacingMd),
                contentAlignment = Alignment.Center,
            ) {
                FeedbackHeroIllustration()
                Text(
                    text = "We value you!",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(16.dp))
                        .background(StitchPrimaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }

            Text(
                text = "How was your experience?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = RrbmuDimens.spacingLg),
            )

            EmojiRatingBar(
                ratings = DefaultEmojiRatings,
                selectedIndex = state.rating.takeIf { it > 0 }?.minus(1),
                onSelected = viewModel::onRatingChange,
            )

            Spacer(modifier = Modifier.height(RrbmuDimens.spacingLg))

            Text(
                text = "QUICK FEEDBACK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FeedbackTagChipRow(
                tags = DefaultFeedbackTags,
                selectedTags = state.selectedTags,
                onTagToggle = viewModel::onTagToggle,
            )

            Spacer(modifier = Modifier.height(RrbmuDimens.spacingLg))

            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::onCommentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = { Text("Tell us more (optional)") },
                shape = RoundedCornerShape(RrbmuDimens.cardRadius),
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(RrbmuDimens.spacingLg))

            Button(
                onClick = viewModel::submit,
                enabled = state.rating > 0 && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RrbmuDimens.buttonRadius),
            ) {
                Text(if (state.isSubmitting) "Submitting…" else "Submit Feedback")
            }

            Text(
                text = "Your feedback helps us build a better RRBMU Studies for everyone.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RrbmuDimens.spacingMd),
            )
        }
    }
}

@Composable
private fun RowHeader(onBack: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "RRBMU Studies",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun FeedbackSuccessScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(RrbmuDimens.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(RrbmuDimens.spacingLg))
        Text(
            text = "Thank you!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "We appreciate your feedback. It helps us build a better learning experience for everyone.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = RrbmuDimens.spacingMd),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RrbmuDimens.cardRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(RrbmuDimens.cardRadius),
                )
                .padding(RrbmuDimens.spacingMd),
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RrbmuDimens.spacingMd),
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = StitchPrimaryContainer)
                Column {
                    Text("Student Impact", fontWeight = FontWeight.Bold, color = StitchPrimaryContainer)
                    Text(
                        "Your voice directly influences our next feature update.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(RrbmuDimens.spacingXl))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(RrbmuDimens.buttonRadius),
        ) {
            Text("Back to More")
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
