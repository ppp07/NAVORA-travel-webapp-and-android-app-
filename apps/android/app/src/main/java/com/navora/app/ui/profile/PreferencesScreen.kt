package com.navora.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.model.TravelerProfile
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraMuted

private val paces = listOf("Relaxed", "Balanced", "Packed", "Spontaneous")
private val walkingLevels = listOf("Minimal", "Moderate", "High")
private val restLevels = listOf("Frequent breaks", "Balanced", "Fewer breaks")
private val interests = listOf(
    "Beaches", "Food", "Culture", "History", "Adventure", "Nature", "Shopping",
    "Nightlife", "Photography", "Architecture", "Wellness", "Local experiences",
    "Spiritual experiences", "Sports",
)
private val foods = listOf(
    "Vegetarian", "Non-vegetarian", "Vegan", "Local cuisine", "Street food",
    "Fine dining", "Cafés", "No strong preference",
)
private val budgets = listOf("Budget", "Moderate", "Premium", "Luxury")
private val stays = listOf("Hostel", "Budget hotel", "Comfortable hotel", "Resort", "Luxury")
private val transports = listOf("Walking", "Public transport", "Taxi/cab", "Rental vehicle", "Train", "Flight", "Combination")
private val activityLevels = listOf("Low", "Moderate", "High")
private val socialStyles = listOf("Solo", "Couple", "Family", "Friends", "Group travel")
private val priorities = listOf(
    "Save money", "Save time", "Maximum comfort", "Maximum experiences",
    "Local experiences", "Relaxation", "Convenience",
)
private val mobilityOptions = listOf(
    "No special requirements",
    "Prefer shorter walking distances",
    "Limited walking",
    "Wheelchair-friendly options preferred",
)
private val accessOptions = listOf(
    "Avoid stairs",
    "Prefer elevators/ramps",
    "Accessible accommodation",
    "Accessible bathrooms",
    "Accessible transportation",
)
private val pacingOptions = listOf(
    "Frequent rest breaks",
    "Fewer activities per day",
    "Longer activity windows",
    "Avoid tightly packed schedules",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    onDone: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val profile = snapshot.profile
    var step by rememberSaveable { mutableIntStateOf(0) }

    fun save(next: TravelerProfile) = viewModel.updateProfile(next)

    NavoraScreen(title = "Preferences", onBack = onDone) {
        Text(
            text = "Step ${step + 1} of 4",
            style = MaterialTheme.typography.labelLarge,
            color = NavoraMuted,
        )
        when (step) {
            0 -> {
                Text("How do you like the days to feel?", style = MaterialTheme.typography.titleLarge)
                ChipRow(paces, profile.style) { save(profile.copy(style = it)) }
                Text("What do you want more of?", style = MaterialTheme.typography.titleMedium)
                MultiChips(interests, profile.interests) { save(profile.copy(interests = it)) }
            }
            1 -> {
                Text("Food, budget, and how you stay", style = MaterialTheme.typography.titleLarge)
                ChipRow(foods, profile.food) { save(profile.copy(food = it)) }
                ChipRow(budgets, profile.budget) { save(profile.copy(budget = it)) }
                ChipRow(stays, profile.accommodation) { save(profile.copy(accommodation = it)) }
                ChipRow(transports, profile.transport) { save(profile.copy(transport = it)) }
            }
            2 -> {
                Text("Who you travel with, and what wins", style = MaterialTheme.typography.titleLarge)
                ChipRow(socialStyles, profile.socialStyle) { save(profile.copy(socialStyle = it)) }
                ChipRow(activityLevels, profile.activityLevel) { save(profile.copy(activityLevel = it)) }
                Text("Walking", style = MaterialTheme.typography.titleMedium)
                ChipRow(walkingLevels, profile.walking) { save(profile.copy(walking = it)) }
                Text("Rest", style = MaterialTheme.typography.titleMedium)
                ChipRow(restLevels, profile.rest) { save(profile.copy(rest = it)) }
                Text("Priorities", style = MaterialTheme.typography.titleMedium)
                MultiChips(priorities, profile.priorities) { save(profile.copy(priorities = it)) }
            }
            else -> {
                Text("Access and pacing", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Optional. Only what you choose is used when a place or a route does not fit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavoraMuted,
                )
                ChipRow(mobilityOptions, profile.mobility) { save(profile.copy(mobility = it)) }
                Text("Access", style = MaterialTheme.typography.titleMedium)
                MultiChips(accessOptions, profile.accessibility) { save(profile.copy(accessibility = it)) }
                Text("Pacing", style = MaterialTheme.typography.titleMedium)
                MultiChips(pacingOptions, profile.pacingNeeds) { save(profile.copy(pacingNeeds = it)) }
            }
        }
        PrimaryButton(
            text = if (step == 3) "Save preferences" else "Continue",
            onClick = {
                if (step == 3) onDone() else step += 1
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(option) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiChips(options: List<String>, selected: List<String>, onChange: (List<String>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = {
                    onChange(if (option in selected) selected - option else selected + option)
                },
                label = { Text(option) },
            )
        }
    }
}
