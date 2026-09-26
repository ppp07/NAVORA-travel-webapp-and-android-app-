package com.navora.app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.model.Destination
import com.navora.app.model.TravelerProfile
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.NavyButton
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.components.wash
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.components.StatusPill
import com.navora.app.ui.theme.NavoraCard
import com.navora.app.ui.theme.NavoraMuted
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.navora.app.R

private val categories = listOf("All", "Nature", "Adventure", "Culture", "Food", "Relaxation")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onPlanDestination: (String) -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    val filtered = snapshot.destinations.filter { destination ->
        val matchesCategory = category == "All" || destination.category == category
        val haystack = "${destination.city} ${destination.country} ${destination.summary}".lowercase()
        val matchesQuery = query.isBlank() || haystack.contains(query.trim().lowercase())
        matchesCategory && matchesQuery
    }
    val recommended = filtered
        .map { it to it.fit(snapshot.profile) }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .take(4)
        .map { it.first }
    val recommendedIds = recommended.map { it.id }.toSet()
    val popular = filtered.filter { it.popular && it.id !in recommendedIds }

    NavoraScreen(title = "Explore") {
        Text(
            text = "Because you like ${snapshot.profile.interests.take(2).joinToString(" and ").lowercase()}.",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search destinations") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { item ->
                FilterChip(
                    selected = category == item,
                    onClick = { category = item },
                    label = { Text(item) },
                )
            }
        }
        DestinationSection(
            title = "Recommended for you",
            detail = "Shaped by your Travel DNA",
            destinations = recommended,
            onPlanDestination = onPlanDestination,
        )
        ExperienceSection(
            destinations = recommended.ifEmpty { filtered.take(3) },
            onPlanDestination = onPlanDestination,
        )
        DestinationSection(
            title = "Popular",
            detail = "Widely travelled, separate from your profile",
            destinations = popular,
            onPlanDestination = onPlanDestination,
        )
        if (filtered.isEmpty()) {
            Text("No destinations match that search.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun destinationImage(city: String): Int = when (city) {
    "Goa" -> R.drawable.dest_goa
    "Jaipur" -> R.drawable.dest_jaipur
    "Manali" -> R.drawable.dest_manali
    "Mumbai" -> R.drawable.dest_mumbai
    "Delhi" -> R.drawable.dest_delhi
    "Paris" -> R.drawable.dest_paris
    "Tokyo" -> R.drawable.dest_tokyo
    "Singapore" -> R.drawable.dest_singapore
    "Dubai" -> R.drawable.dest_dubai
    "Bangkok" -> R.drawable.dest_bangkok
    "Bali" -> R.drawable.dest_bali
    "London" -> R.drawable.dest_london
    "Switzerland" -> R.drawable.dest_switzerland
    "Kerala" -> R.drawable.dest_kerala
    "Udaipur" -> R.drawable.dest_udaipur
    "Rishikesh" -> R.drawable.dest_rishikesh
    "Kashmir" -> R.drawable.dest_kashmir
    else -> R.drawable.dest_goa
}

private fun Destination.fit(profile: TravelerProfile): Int {
    val interests = profile.interests.map { it.lowercase() }
    var score = tags.count { tag ->
        interests.any { interest -> interest.contains(tag) || tag.contains(interest) }
    }
    if (profile.style == "Fast-paced" && "adventure" in tags) score += 2
    if (profile.style == "Relaxed" && "relaxation" in tags) score += 2
    if (profile.food == "Local cuisine" && "food" in tags) score += 1
    return score
}

@Composable
private fun ExperienceSection(
    destinations: List<Destination>,
    onPlanDestination: (String) -> Unit,
) {
    if (destinations.isEmpty()) return
    Text(text = "Experiences for you", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "Matched to your Travel DNA",
        style = MaterialTheme.typography.bodyMedium,
        color = NavoraMuted,
    )
    destinations.take(3).forEachIndexed { index, destination ->
        val match = 72 - index * 6
        SoftCard {
            Box {
                Image(
                    painter = painterResource(destinationImage(destination.city)),
                    contentDescription = destination.city,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                StatusPill("$match% match", NavoraCard)
            }
            Text(
                text = destination.category.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "${destination.city}, ${destination.country}",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
            Text(
                text = destination.city,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = destination.summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "Why it matches  ·  ${destination.tags.take(2).joinToString(" and ").ifBlank { destination.category }}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = destination.tags.take(3).joinToString("   "),
                style = MaterialTheme.typography.labelLarge,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
            PrimaryButton(
                text = "Add to journey",
                onClick = { onPlanDestination(destination.city) },
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun DestinationSection(
    title: String,
    detail: String,
    destinations: List<Destination>,
    onPlanDestination: (String) -> Unit,
) {
    if (destinations.isEmpty()) return
    Text(text = title, style = MaterialTheme.typography.titleLarge)
    Text(
        text = detail,
        style = MaterialTheme.typography.bodyMedium,
        color = NavoraMuted,
    )
    destinations.forEach { destination ->
        SoftCard {
            Image(
                painter = painterResource(destinationImage(destination.city)),
                contentDescription = destination.city,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Text(
                text = destination.category,
                style = MaterialTheme.typography.labelLarge,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = destination.city,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = destination.country,
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
            Text(
                text = destination.summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            NavyButton(
                text = "Plan this trip",
                onClick = { onPlanDestination(destination.city) },
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}
