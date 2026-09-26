package com.navora.app.ui.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.model.ItineraryDay
import com.navora.app.model.TripActivity
import com.navora.app.model.TripEvent
import com.navora.app.ui.components.ActivityTimeline
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.components.StatusPill
import com.navora.app.ui.components.label
import com.navora.app.ui.components.wash
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraMint
import com.navora.app.ui.theme.NavoraMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItineraryScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenAdaptation: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val trip = snapshot.trips.firstOrNull { it.id == tripId }
    if (trip == null) {
        NavoraScreen(title = "Itinerary", onBack = onBack) {
            Text("This trip could not be found.")
        }
        return
    }
    val scenario = snapshot.scenarios.firstOrNull { it.tripId == trip.id }
    var selectedDay by remember { mutableIntStateOf(0) }
    val day = trip.days[selectedDay.coerceIn(0, trip.days.lastIndex)]
    val estimate = estimateCost(trip.budget, trip.days.size)
    val fit = (82 + (trip.progress * 12).toInt()).coerceIn(82, 96)

    NavoraScreen(title = trip.destination, onBack = onBack) {
        Text(text = trip.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "From ${trip.origin}  ·  ${trip.days.size} days  ·  ${trip.startDate} – ${trip.endDate}",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        Text(
            text = "Estimated ${formatRupees(estimate)}  ·  Journey fit $fit%",
            style = MaterialTheme.typography.titleMedium,
        )
        StatusPill(trip.status.label(), trip.tone.wash())
        if (trip.styles.isNotEmpty()) {
            Text(
                text = trip.styles.joinToString("  ·  "),
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
        }
        Text("Adapt this trip", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                TripEvent.HeavyRain,
                TripEvent.ExtremeHeat,
                TripEvent.Storm,
                TripEvent.FlightDelay,
                TripEvent.TrainDelay,
                TripEvent.Traffic,
                TripEvent.RoadClosure,
                TripEvent.AttractionClosed,
                TripEvent.Crowded,
                TripEvent.FeelingSick,
                TripEvent.LowEnergy,
                TripEvent.MoreRest,
                TripEvent.SpendLess,
                TripEvent.SpendMore,
                TripEvent.MissedActivity,
                TripEvent.SlowerDay,
                TripEvent.AddActivities,
            ).forEach { event ->
                FilterChip(
                    selected = scenario?.eventLabel == event.label,
                    onClick = {
                        viewModel.adaptTo(trip.id, event.name)
                        onOpenAdaptation()
                    },
                    label = { Text(event.label) },
                )
            }
        }
        if (scenario != null) {
            SoftCard(
                container = if (scenario.applied) NavoraMint else NavoraLavender,
                onClick = onOpenAdaptation,
            ) {
                Text(
                    text = if (scenario.applied) "TripSaathi updated this plan" else "A change is waiting",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = scenario.changeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = if (scenario.applied) "See what moved" else "Compare the original and the adapted day",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        Text("Days", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            trip.days.forEachIndexed { index, item ->
                FilterChip(
                    selected = index == selectedDay,
                    onClick = { selectedDay = index },
                    label = { Text("Day ${item.dayNumber}") },
                )
            }
        }
        DayBlock(day) { activity -> viewModel.chooseAlternative(trip.id, activity.id) }
        if (scenario != null && !scenario.applied) {
            PrimaryButton(text = "Review the change", onClick = onOpenAdaptation)
        }
    }
}

private fun estimateCost(budget: String, days: Int): Int {
    val perDay = when (budget) {
        "Budget", "Value" -> 2800
        "Premium", "Luxury", "Comfortable" -> 8200
        else -> 4600
    }
    return perDay * days.coerceAtLeast(1)
}

private fun formatRupees(amount: Int): String {
    val digits = amount.toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "₹$grouped"
}

@Composable
private fun DayBlock(day: ItineraryDay, onChooseAlternative: (TripActivity) -> Unit) {
    SoftCard {
        Text(
            text = "Day ${day.dayNumber}  ·  ${day.dateLabel}",
            style = MaterialTheme.typography.labelLarge,
            color = NavoraMuted,
        )
        Text(
            text = day.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
        )
        ActivityTimeline(day.activities, onChooseAlternative = onChooseAlternative)
        if (day.recommendation != null) {
            Text(
                text = "Why this day",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = day.recommendation,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
