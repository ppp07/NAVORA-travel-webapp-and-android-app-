package com.navora.app.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.model.TripActivity
import com.navora.app.ui.components.ActivityTimeline
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraCoral
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraMint
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraPeach

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdaptiveChangeScreen(
    tripId: String,
    onBack: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val trip = snapshot.trips.firstOrNull { it.id == tripId }
    val scenario = snapshot.scenarios.firstOrNull { it.tripId == tripId }
    if (trip == null || scenario == null) {
        NavoraScreen(title = "Adaptation", onBack = onBack) {
            Text("This trip does not have an adaptive change yet.")
        }
        return
    }

    NavoraScreen(title = "Adaptation", onBack = onBack) {
        Text(text = scenario.headline, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "${trip.destination}  ·  Day ${scenario.dayNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        if (scenario.affectedTitle.isNotBlank()) {
            Text(
                text = scenario.affectedTitle,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (scenario.unchanged.isNotEmpty()) {
            Text(
                text = "Stays as planned: ${scenario.unchanged.take(4).joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
        }
        val changedFrom = scenario.before.firstOrNull { it.title == scenario.affectedTitle }?.time ?: "15:00"
        PlanColumn(
            label = "Before",
            detail = "Day ${scenario.dayNumber}",
            activities = scenario.before.from(changedFrom),
            wash = NavoraPeach,
        )
        SoftCard(container = NavoraCoral) {
            Text(text = "Change detected", style = MaterialTheme.typography.labelLarge)
            Text(
                text = scenario.changeLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = scenario.changeDetail,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        SoftCard {
            Text(text = "What this change touches", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                RippleChip("${scenario.ripple.activities} activity")
                RippleChip("${scenario.ripple.travelLinks} travel links")
                RippleChip("${scenario.ripple.meals} meal")
                RippleChip("${scenario.ripple.eveningPlans} evening plan")
            }
        }
        SoftCard(container = NavoraMint) {
            Text(text = "TripSaathi adapts", style = MaterialTheme.typography.labelLarge)
            Text(
                text = scenario.adaptationDetail,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (scenario.tradeOffs.isNotEmpty()) {
            Text(text = "Two ways to adjust", style = MaterialTheme.typography.titleMedium)
            scenario.tradeOffs.forEachIndexed { index, option ->
                SoftCard(onClick = { viewModel.selectTradeOff(tripId, index) }) {
                    Text(
                        text = if (scenario.selectedOption == index) "Selected  ·  ${option.label}" else option.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${option.cost}  ·  ${option.travel}  ·  ${option.comfort}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        text = option.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavoraMuted,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (scenario.accessibilityNote != null) {
            SoftCard(container = NavoraLavender) {
                Text(text = "Access", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = scenario.accessibilityNote,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        PrimaryButton(
            text = if (scenario.applied) "Updated plan applied" else "Apply updated plan",
            onClick = { viewModel.applyAdaptation(tripId) },
            enabled = !scenario.applied,
        )
        val updated = if (scenario.selectedOption == 1 && scenario.alternateAfter.isNotEmpty()) {
            scenario.alternateAfter
        } else {
            scenario.after
        }
        PlanColumn(
            label = "Updated plan",
            detail = "Revised afternoon",
            activities = updated.from(changedFrom),
            wash = NavoraMint,
        )
        if (scenario.applied) {
            Text(
                text = "The day in your itinerary now matches this revision. Earlier stops that were still sound were left alone.",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
            )
        }
    }
}

@Composable
private fun RippleChip(label: String) {
    FilterChip(selected = false, onClick = {}, label = { Text(label) })
}

private fun List<TripActivity>.from(start: String): List<TripActivity> = filter { it.time >= start }

@Composable
private fun PlanColumn(
    label: String,
    detail: String,
    activities: List<TripActivity>,
    wash: androidx.compose.ui.graphics.Color,
) {
    SoftCard(container = wash) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = detail,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )
        ActivityTimeline(activities = activities, compact = true)
    }
}
