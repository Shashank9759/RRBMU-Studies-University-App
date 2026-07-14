package com.studies.rrbmustudies.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.ui.theme.StitchOnSecondaryFixed
import com.studies.rrbmustudies.ui.theme.StitchSecondaryFixed
import com.studies.rrbmustudies.ui.theme.courseAccentColor
import com.studies.rrbmustudies.ui.theme.courseBorderAccent
import com.studies.rrbmustudies.ui.theme.courseJewel
import com.studies.rrbmustudies.ui.theme.levelDuration
import com.studies.rrbmustudies.ui.theme.levelFieldTag
import com.studies.rrbmustudies.ui.theme.rememberPressScale

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuickAccessCourseCard(course = course, onClick = onClick, modifier = modifier)
}

@Composable
fun QuickAccessCourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jewel = courseJewel(course.shortName)
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(rememberPressScale(interaction), label = "tileScale")
    val subtitle = courseFieldSubtitle(course)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = jewel.glow)
            .clip(RoundedCornerShape(24.dp))
            .background(jewel.brush)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                onClick = onClick,
            )
            .heightIn(min = 132.dp),
    ) {
        CrestWatermark(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(96.dp)
                .graphicsLayer { alpha = 0.12f },
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = course.shortName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = levelFieldTag(course.level),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A short, human field label for a tile subtitle (e.g. "Science & Tech"). */
private fun courseFieldSubtitle(course: Course): String {
    val n = course.name
    return when {
        n.contains("Science", true) -> "Science & Tech"
        n.contains("Arts", true) -> "Arts & Humanities"
        n.contains("Commerce", true) -> "Commerce"
        n.contains("Computer", true) -> "Applications"
        n.contains("Business", true) -> "Management"
        n.contains("Education", true) -> "Education"
        n.contains("Law", true) || n.contains("LLB", true) -> "Law"
        n.contains("Dental", true) -> "Medical"
        else -> n.removePrefix("Bachelor of ").removePrefix("Master of ")
    }
}

@Composable
fun CourseListRow(
    course: Course,
    onClick: () -> Unit,
    paperCountLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableRow(onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = courseJewel(course.shortName).glow)
                .clip(RoundedCornerShape(16.dp))
                .background(courseJewel(course.shortName).brush),
            contentAlignment = Alignment.Center,
        ) {
            if (course.iconUrl != null) {
                AsyncImage(
                    model = course.iconUrl,
                    contentDescription = course.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = course.shortName.take(4).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = levelFieldTag(course.level),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
                Text(
                    text = levelDuration(course.level),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        if (paperCountLabel != null) {
            Text(
                text = paperCountLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = StitchOnSecondaryFixed,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(StitchSecondaryFixed)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
fun PartExploreCard(
    partName: String,
    description: String,
    paperCount: Int,
    paperCountLabel: String,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    partNumber: Int = 1,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF0A1046))
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onExplore)
            .padding(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = partNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Text(
                text = "RRBMU CURRICULUM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = partName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatRing(value = paperCount.toString())
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$paperCount Papers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = paperCountLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SaffronButton(
                text = if (highlighted) "View Papers" else "Explore",
                onClick = onExplore,
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
