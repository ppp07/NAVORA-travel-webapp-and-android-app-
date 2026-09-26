package com.navora.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.R
import com.navora.app.model.DemoSnapshot
import com.navora.app.model.TravelerProfile
import com.navora.app.model.Trip
import com.navora.app.model.TripStatus
import com.navora.app.ui.components.DnaBars
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.components.StatusPill
import com.navora.app.ui.components.TripProgress
import com.navora.app.ui.components.label
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraBlue
import com.navora.app.ui.theme.NavoraCoralAccent
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraMint
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy
import com.navora.app.ui.theme.NavoraPeach
import com.navora.app.ui.theme.NavoraWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onPlanTrip: () -> Unit,
    onOpenTrips: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenTrip: (String) -> Unit,
    onOpenAdaptation: (String) -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val featured = snapshot.featuredTrip()
    val scenario = featured?.let { trip -> snapshot.scenarios.firstOrNull { it.tripId == trip.id } }
    val firstName = snapshot.profile.name.substringBefore(" ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "NAVORA",
            style = MaterialTheme.typography.labelLarge,
            color = NavoraCoralAccent,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${greeting()}, $firstName",
            style = MaterialTheme.typography.headlineMedium,
            color = NavoraNavy,
        )
        Text(
            text = personalLine(snapshot.profile, featured),
            style = MaterialTheme.typography.bodyLarge,
            color = NavoraMuted,
        )
        if (featured != null) {
            FeaturedTripCard(trip = featured, onOpen = { onOpenTrip(featured.id) })
        }
        SoftCard(onClick = onOpenPreferences) {
            Text(
                text = "Your Travel DNA",
                style = MaterialTheme.typography.titleLarge,
                color = NavoraNavy,
            )
            Text(
                text = "Navora learns what makes a trip feel right for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            )
            DnaBars(snapshot.profile.dna())
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 14.dp),
            ) {
                DnaChip("${snapshot.profile.style} pace")
                DnaChip("${snapshot.profile.budget} budget")
                DnaChip("${snapshot.profile.accommodation} comfort")
                DnaChip("${snapshot.profile.walking} walking")
                DnaChip("${snapshot.profile.rest} rest")
                snapshot.profile.interests.take(3).forEach { DnaChip(it) }
            }
        }
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleLarge,
            color = NavoraNavy,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Plan a Trip", Icons.Outlined.AutoAwesome, NavoraPeach, Modifier.weight(1f), onPlanTrip)
            QuickAction("My Trips", Icons.Outlined.CardTravel, NavoraBlue, Modifier.weight(1f), onOpenTrips)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Explore", Icons.Outlined.Explore, NavoraMint, Modifier.weight(1f), onOpenExplore)
            QuickAction("Preferences", Icons.Outlined.Tune, NavoraLavender, Modifier.weight(1f), onOpenPreferences)
        }
        SoftCard(
            container = NavoraLavender,
            onClick = { featured?.let { onOpenAdaptation(it.id) } ?: onOpenPreferences() },
        ) {
            Text(
                text = "Adaptive Intelligence",
                style = MaterialTheme.typography.titleLarge,
                color = NavoraNavy,
            )
            Text(
                text = if (featured != null && scenario != null && !scenario.applied) {
                    "${featured.destination} · ${scenario.changeLabel.trimEnd('.')}."
                } else {
                    "Navora understands how your choices affect the rest of your journey. When plans change, it can adapt the affected parts of your trip instead of making you rebuild everything."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraNavy,
                modifier = Modifier.padding(top = 8.dp),
            )
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(
                    "Preferences",
                    "Trip plan",
                    "Change detected",
                    "Affected plans identified",
                    "Itinerary adapted",
                ).forEachIndexed { index, step ->
                    Text(
                        text = if (index == 0) step else "↓  $step",
                        style = MaterialTheme.typography.labelLarge,
                        color = NavoraNavy,
                    )
                }
            }
            Text(
                text = if (scenario?.applied == true) "Updated plan applied" else "See how it works",
                style = MaterialTheme.typography.titleMedium,
                color = NavoraCoralAccent,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun FeaturedTripCard(trip: Trip, onOpen: () -> Unit) {
    SoftCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Image(
                painter = painterResource(destinationImage(trip.destination)),
                contentDescription = trip.destination,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, NavoraNavy.copy(alpha = 0.72f)),
                        ),
                    ),
            )
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(text = trip.destination, style = MaterialTheme.typography.labelLarge, color = NavoraWhite)
                Text(text = trip.title, style = MaterialTheme.typography.headlineSmall, color = NavoraWhite)
            }
        }
        Text(
            text = listOf(stayLabel(trip), trip.interests.take(3).joinToString(" + "))
                .filter { it.isNotBlank() }
                .joinToString("  ·  "),
            style = MaterialTheme.typography.titleMedium,
            color = NavoraNavy,
            modifier = Modifier.padding(top = 14.dp),
        )
        if (trip.routeSummary.isNotBlank()) {
            Text(
                text = trip.routeSummary,
                style = MaterialTheme.typography.bodyLarge,
                color = NavoraNavy,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = "${trip.startDate} – ${trip.endDate}",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(trip.status.label(), NavoraPeach)
            Text(
                text = "${trip.travelers} travelers",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
        }
        Text(
            text = "Trip progress",
            style = MaterialTheme.typography.labelLarge,
            color = NavoraMuted,
            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
        )
        TripProgress(trip.progress)
        PrimaryButton(
            text = if (trip.status == TripStatus.InProgress) "Continue trip" else "View itinerary",
            onClick = onOpen,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun DnaChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = NavoraNavy,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(NavoraPeach)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    wash: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    SoftCard(modifier = modifier, onClick = onClick, container = wash) {
        Icon(
            icon,
            contentDescription = null,
            tint = NavoraNavy,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = NavoraNavy,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun personalLine(profile: TravelerProfile, trip: Trip?): String {
    if (trip == null) return "Ready for your next journey?"
    val comfortFirst = profile.style == "Relaxed" || profile.walking == "Minimal" ||
        profile.priorities.any { it.contains("comfort", ignoreCase = true) }
    return when {
        trip.status == TripStatus.InProgress -> {
            val left = ((1f - trip.progress) * trip.days.size).toInt().coerceAtLeast(1)
            "$left days left in your ${trip.destination} journey."
        }
        comfortFirst -> "Your comfort-first ${trip.destination} journey is ready."
        else -> "Your ${profile.style.lowercase()} ${trip.destination} journey is ready."
    }
}

private fun stayLabel(trip: Trip): String {
    val days = trip.days.size
    val nights = (days - 1).coerceAtLeast(0)
    val dayWord = if (days == 1) "Day" else "Days"
    val nightWord = if (nights == 1) "Night" else "Nights"
    return "$days $dayWord • $nights $nightWord"
}

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

private fun DemoSnapshot.featuredTrip(): Trip? {
    return trips.firstOrNull { it.status == TripStatus.InProgress }
        ?: trips.firstOrNull { it.status == TripStatus.Upcoming }
        ?: trips.firstOrNull()
}
