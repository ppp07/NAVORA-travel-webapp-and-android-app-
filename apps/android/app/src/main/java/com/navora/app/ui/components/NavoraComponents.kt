package com.navora.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navora.app.model.ActivityKind
import com.navora.app.model.DestinationTone
import com.navora.app.model.TripActivity
import com.navora.app.model.TripStatus
import com.navora.app.ui.theme.NavoraBlue
import com.navora.app.ui.theme.NavoraCard
import com.navora.app.ui.theme.NavoraCoral
import com.navora.app.ui.theme.NavoraCoralAccent
import com.navora.app.ui.theme.NavoraCream
import com.navora.app.ui.theme.NavoraCreamDeep
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraLine
import com.navora.app.ui.theme.NavoraMint
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy
import com.navora.app.ui.theme.NavoraOrange
import com.navora.app.ui.theme.NavoraPeach
import com.navora.app.ui.theme.NavoraWhite

fun DestinationTone.wash(): Color = when (this) {
    DestinationTone.Blue -> NavoraBlue
    DestinationTone.Lavender -> NavoraLavender
    DestinationTone.Peach -> NavoraPeach
    DestinationTone.Mint -> NavoraMint
    DestinationTone.Coral -> NavoraCoral
}

fun TripStatus.label(): String = when (this) {
    TripStatus.InProgress -> "In progress"
    TripStatus.Upcoming -> "Upcoming"
    TripStatus.Completed -> "Completed"
}

fun ActivityKind.wash(): Color = when (this) {
    ActivityKind.Meal -> NavoraPeach
    ActivityKind.Place -> NavoraBlue
    ActivityKind.Transit -> NavoraMint
    ActivityKind.Experience -> NavoraLavender
}

fun ActivityKind.label(): String = when (this) {
    ActivityKind.Meal -> "Food"
    ActivityKind.Place -> "Place"
    ActivityKind.Transit -> "Transit"
    ActivityKind.Experience -> "Experience"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavoraTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
) {
    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavoraCream,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
fun NavoraScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NavoraTopBar(title = title, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val colors = CardDefaults.cardColors(containerColor = container)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val cardModifier = modifier
        .fillMaxWidth()
        .border(1.dp, NavoraLine, shape)
    if (onClick == null) {
        Card(modifier = cardModifier, shape = shape, colors = colors, elevation = elevation) {
            Column(modifier = Modifier.padding(18.dp), content = content)
        }
    } else {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            Column(modifier = Modifier.padding(18.dp), content = content)
        }
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NavoraOrange),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Explore,
                contentDescription = null,
                tint = NavoraNavy,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = "TripSaathi",
            style = MaterialTheme.typography.titleMedium,
            color = NavoraNavy,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun BrandChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    filled: Boolean = true,
) {
    val selectedColor = if (filled) NavoraCoralAccent else NavoraNavy
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(999.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor,
            selectedLabelColor = NavoraWhite,
            containerColor = NavoraCard,
            labelColor = NavoraNavy,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = NavoraLine,
            selectedBorderColor = selectedColor,
        ),
    )
}

@Composable
fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = NavoraMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = NavoraNavy,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavoraOrange,
            contentColor = NavoraNavy,
            disabledContainerColor = NavoraCreamDeep,
            disabledContentColor = NavoraMuted,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun NavyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NavoraNavy,
            contentColor = NavoraWhite,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 6.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun StatusPill(text: String, container: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
fun TripProgress(progress: Float) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp)),
        color = NavoraOrange,
        trackColor = NavoraLine,
    )
}

@Composable
fun ActivityTimeline(
    activities: List<TripActivity>,
    compact: Boolean = false,
    onChooseAlternative: ((TripActivity) -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        activities.forEachIndexed { index, activity ->
            ActivityRow(
                activity = activity,
                showLine = index < activities.lastIndex,
                compact = compact,
                onChooseAlternative = onChooseAlternative,
            )
        }
    }
}

@Composable
private fun ActivityRow(
    activity: TripActivity,
    showLine: Boolean,
    compact: Boolean,
    onChooseAlternative: ((TripActivity) -> Unit)?,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(activity.kind.wash()),
            )
            if (showLine) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(2.dp)
                        .height(if (compact) 28.dp else 72.dp)
                        .background(NavoraLine),
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .padding(bottom = if (showLine) 8.dp else 0.dp),
        ) {
            Text(
                text = "${activity.time}  ·  ${activity.duration}",
                style = MaterialTheme.typography.labelLarge,
                color = NavoraMuted,
            )
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "${activity.place}  ·  ${activity.kind.label()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!compact && activity.reason.isNotBlank()) {
                Text(
                    text = activity.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavoraNavy,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (!compact) {
                Text(
                    text = activity.note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (activity.conflict != null) {
                Text(
                    text = activity.conflict,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavoraCoralAccent,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (onChooseAlternative != null && activity.alternativeTitle != null) {
                    TextButton(onClick = { onChooseAlternative(activity) }) {
                        Text("Choose ${activity.alternativeTitle}")
                    }
                }
            }
        }
    }
}

@Composable
fun DnaBars(bars: List<Pair<String, Float>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        bars.forEach { (label, level) ->
            Column {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                TripProgress(level)
            }
        }
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
